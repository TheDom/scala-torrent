package com.dominikgruber.scalatorrent.actor

import akka.actor.{ActorRef, Actor}
import akka.io.Tcp._
import akka.io.{Tcp, IO}
import akka.util.ByteString
import java.net.InetSocketAddress

import com.dominikgruber.scalatorrent.metainfo.Metainfo
import com.dominikgruber.scalatorrent.peerwireprotocol.Handshake

object PeerConnection {
  case class AttachToTorrent(torrent: ActorRef, metainfo: Metainfo)
  case class SendHandshake()
}

class PeerConnection(remoteAddress: InetSocketAddress, internalPeerId: String, coordinator: ActorRef) extends Actor {
  import PeerConnection._
  import Coordinator._
  import context.system

  var connection: Option[ActorRef] = None
  var torrent: Option[ActorRef] = None
  var metainfo: Option[Metainfo] = None

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

  override def receive: Receive = initialStage

  def initialStage: Receive = {
    case AttachToTorrent(t, m) =>
      torrent = Some(t)
      metainfo = Some(m)

    case SendHandshake if torrent.isDefined =>
      if (connection.isDefined) sendHandshake()
      else {
        IO(Tcp) ! Connect(remoteAddress)
        context become awaitConnectionForHandshake
      }

    case Received(data) => handleHandshakeIn(data)
    case PeerClosed => handlePeerClosed()
  }

  def awaitConnectionForHandshake: Receive = {
    case c @ Connected(_, _) =>
      connection = Some(context.sender())
      connection.get ! Register(self)
      sendHandshake()
      context become awaitHandshakeIn

    case CommandFailed(_: Connect) =>
      // TODO: Handle failure
  }

  def awaitHandshakeIn: Receive = {
    case Received(data) => handleHandshakeIn(data)
    case PeerClosed => handlePeerClosed()
  }

  def connected: Receive = {
    case Received(data) =>
      // TODO: Handle failure

    case PeerClosed => handlePeerClosed()
  }

  def handleHandshakeIn(data: ByteString) = {
    Handshake.unmarshal(data.toVector) match {
      case Some(handshake: Handshake) =>
        connection = Some(context.sender())
        if (torrent.isDefined) context become connected
        else coordinator ! IncomingPeerConnection(self, handshake)

      case None =>
        // TODO: Handle failure
    }
  }

  def sendHandshake() = {
    val handshake = Handshake(metainfo.get.info.infoHash, internalPeerId)
    connection.get ! Write(ByteString(handshake.marshal.toArray))
  }

  def handlePeerClosed() = {
    // TODO
    // context stop self
  }
}