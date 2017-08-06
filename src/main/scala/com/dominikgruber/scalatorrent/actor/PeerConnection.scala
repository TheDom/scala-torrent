package com.dominikgruber.scalatorrent.actor

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.dominikgruber.scalatorrent.actor.Coordinator.{IdentifyTorrent, TorrentInfo}
import com.dominikgruber.scalatorrent.actor.PeerConnection.{BeginConnection, ReceiveConnection, Send}
import com.dominikgruber.scalatorrent.actor.Torrent.{AreWeInterested, NextRequest}
import com.dominikgruber.scalatorrent.metainfo.MetaInfo
import com.dominikgruber.scalatorrent.peerwireprotocol.{Message, _}
import com.dominikgruber.scalatorrent.transfer.BitSetUtil

object PeerConnection {
  case class BeginConnection(torrent: ActorRef, metaInfo: MetaInfo)
  case class ReceiveConnection(tcpConnection: ActorRef)
  case class Send(message: Message)
}

class PeerConnection(remoteAddress: InetSocketAddress, selfPeerId: String, coordinator: ActorRef) extends Actor with ActorLogging {
  import context._

  /**
   * Whether or not the remote peer has choked this client. When a peer chokes
   * the client, it is a notification that no requests will be answered until
   * the client is unchoked. The client should not attempt to send requests for
   * blocks, and it should consider all pending (unanswered) requests to be
   * discarded by the remote peer.
   */
  var peerChoking = true
  var amChoking = true

  /**
   * Whether or not the remote peer is interested in something this client has
   * to offer. This is a notification that the remote peer will begin requesting
   * blocks when the client unchokes them.
   */
  var peerInterested = false
  var amInterested = false

  override def receive: Receive = HandshakeBehavior()

  def connected(tcp: ActorRef, metaInfo: MetaInfo, torrent: ActorRef): Receive = {
    var bitfield: Vector[Boolean] = Vector.fill(metaInfo.fileInfo.numPieces)(false)

    def apply: PartialFunction[Any, Unit] = {
      case Send(msg) => // from Torrent
        log.debug(s"Sending $msg")
        tcp ! Write(ByteString(msg.marshal.toArray))

      case Received(data) => // from Tcp
        Message.unmarshal(data.toVector) match {
          case Some(message) =>
            log.debug(s"Received $message")
            handleMessage(torrent, message)
          case None =>
            log.warning(s"Received unknown message: ${Hex(data)}")
          //TODO parse failure
        }

      case PeerClosed => handlePeerClosed() // from Tcp
    }: PartialFunction[Any, Unit]

    def handleMessage(torrent: ActorRef, message: Message) = message match {
      case p: Piece =>
        torrent ! p
      case b: Bitfield =>
        bitfield = b.downloadedPieces
        torrent ! AreWeInterested(BitSetUtil.fromBooleans(bitfield))
      case Have(index) =>
        bitfield = bitfield.updated(index, true)
        if(!amInterested)
          torrent ! AreWeInterested(BitSetUtil.fromBooleans(bitfield))
      case _: Unchoke =>
        peerChoking = false
        torrent ! NextRequest(BitSetUtil.fromBooleans(bitfield))
      case _ => //TODO
    }

    apply
  }

  object HandshakeBehavior {

    def apply(): Receive = {
      case BeginConnection(torrent, metaInfo) => // from Torrent
        IO(Tcp) ! Connect(remoteAddress)
        context become Begin(metaInfo, torrent)
      case ReceiveConnection(tcp) => // from Coordinator
        context become Respond(tcp)
    }

    object Begin {
      def apply(metaInfo: MetaInfo, torrent: ActorRef): Receive =
        waitTcpConnection(metaInfo, torrent)

      private def waitTcpConnection(metaInfo: MetaInfo, torrent: ActorRef): Receive = {
        case Connected(_, _) => // from Tcp
          sender ! Register(self)
          sendHandshake(sender, metaInfo)
          context become waitSecondHandshake(sender, metaInfo, torrent)

        case CommandFailed(_: Connect) => // from Tcp
          log.error("Failed to open TCP connection")
          //TODO: Handle failure
      }

      private def waitSecondHandshake(tcp: ActorRef, metaInfo: MetaInfo, torrent: ActorRef): Receive = {
        case Received(data) => // from Tcp
          Handshake.unmarshall(data.toVector) match {
            case Some(h: Handshake) =>
              log.debug(s"Received 2nd handshake: $h")
              //TODO validate handshake
              context become connected(tcp, metaInfo, torrent)
            case None =>
              log.warning(s"Failed to parse 2nd handshake: ${Hex(data)}")
              //TODO drop peer
          }
        case PeerClosed => handlePeerClosed() // from Tcp
      }

    }

    object Respond {
      def apply(tcp: ActorRef): Receive = waitFirstHandshake(tcp)

      private def waitFirstHandshake(tcp: ActorRef): Receive = {
        case Received(data) => // from Tcp
          Handshake.unmarshall(data.toVector) match {
            case Some(h: Handshake) =>
              log.debug(s"Received 1st handshake: $h")
              coordinator ! IdentifyTorrent(h.infoHashString) //TODO "ask"
              context become waitTorrentInfo(h, tcp)
            case None =>
              log.warning(s"Failed to parse 1st handshake: ${Hex(data)}")
              //TODO drop peer
          }
        case PeerClosed => handlePeerClosed() // from Tcp
      }

      private def waitTorrentInfo(h: Handshake, tcp: ActorRef): Receive = {
        case TorrentInfo(metaInfo, torrent) => // from Coordinator
          sendHandshake(tcp, metaInfo)
          context become connected(tcp, metaInfo, torrent)
      }
    }

    private def sendHandshake(tcp: ActorRef, metaInfo: MetaInfo) = {
      val handshake = Handshake(metaInfo.fileInfo.infoHash, selfPeerId)
      tcp ! Write(ByteString(handshake.marshal.toArray))
    }

  }

  private def handlePeerClosed(): Unit = {
    log.warning("Peer closed")
//    context stop self
  }

}

object Hex {
  def apply(buf: Array[Byte]): String = buf.map("%02X" format _).mkString(" ")
  def apply(buf: ByteString): String = apply(buf.toArray)
}
