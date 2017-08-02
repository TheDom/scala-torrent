package com.dominikgruber.scalatorrent.actor

import akka.actor.{Actor, ActorRef}
import akka.io.Tcp._
import akka.io.{IO, Tcp}
import akka.util.ByteString
import java.net.InetSocketAddress

import com.dominikgruber.scalatorrent.actor.Coordinator.{IdentifyTorrent, TorrentInfo}
import com.dominikgruber.scalatorrent.actor.PeerConnection.{BeginConnection, PeerConnected, ReceiveConnection}
import com.dominikgruber.scalatorrent.metainfo.MetaInfo
import com.dominikgruber.scalatorrent.peerwireprotocol.Handshake

object PeerConnection {
  case class BeginConnection(torrent: ActorRef, metaInfo: MetaInfo)
  case class ReceiveConnection(tcpConnection: ActorRef)
  case class PeerConnected(peerConnection: ActorRef)
}

class PeerConnection(remoteAddress: InetSocketAddress, internalPeerId: String, coordinator: ActorRef) extends Actor {
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

  override def receive: Receive = {
    case BeginConnection(torrent, metaInfo) => // from Torrent
      IO(Tcp) ! Connect(remoteAddress)
      context become HandshakeSequence.Begin(metaInfo, torrent)

    case ReceiveConnection(tcp) => // from Coordinator
      context become HandshakeSequence.Respond(tcp)
  }

  def connected(tcp: ActorRef, metaInfo: MetaInfo, torrent: ActorRef): Receive = {
    case Received(data) => // from Tcp
      // TODO: Handle failure

    case PeerClosed => handlePeerClosed() // from Tcp
  }

  object HandshakeSequence {

    object Begin {
      def apply(metaInfo: MetaInfo, torrent: ActorRef) =
        waitTcpConnection(metaInfo, torrent)

      private def waitTcpConnection(metaInfo: MetaInfo, torrent: ActorRef): Receive = {
        case Connected(_, _) => // from Tcp
          sender ! Register(self)
          sendHandshake(sender, metaInfo)
          context become waitSecondHandshake(sender, metaInfo, torrent)

        case CommandFailed(_: Connect) => // from Tcp // TODO: Handle failure
      }

      private def waitSecondHandshake(tcp: ActorRef, metaInfo: MetaInfo, torrent: ActorRef): Receive = {
        case Received(data) => // from Tcp
          Handshake.parse(data.toVector) match {
            case Some(handshake: Handshake) =>
              //TODO validate handshake
              torrent ! PeerConnected(self)
              context become connected(tcp, metaInfo, torrent)
            case None => // TODO: Handle failure
          }
        case PeerClosed => handlePeerClosed() // from Tcp
      }

    }

    object Respond {
      def apply(tcp: ActorRef) = waitFirstHandshake(tcp)

      private def waitFirstHandshake(tcp: ActorRef): Receive = {
        case Received(data) => // from Tcp
          Handshake.parse(data.toVector) match {
            case Some(handshake: Handshake) =>
              coordinator ! IdentifyTorrent(handshake.infoHashString)
              context become waitTorrentInfo(tcp)
            case None => // TODO: Handle failure
          }
        case PeerClosed => handlePeerClosed() // from Tcp
      }

      private def waitTorrentInfo(tcp: ActorRef): Receive = {
        case TorrentInfo(metaInfo, torrent) => // from Coordinator
          sendHandshake(tcp, metaInfo)
          torrent ! PeerConnected(self)
          context become connected(tcp, metaInfo, torrent)
      }
    }

    private def sendHandshake(tcp: ActorRef, metaInfo: MetaInfo) = {
      val handshake = Handshake(metaInfo.fileInfo.infoHash, internalPeerId)
      tcp ! Write(ByteString(handshake.marshal.toArray))
    }

  }

  private def handlePeerClosed() = () // TODO context stop self

}
