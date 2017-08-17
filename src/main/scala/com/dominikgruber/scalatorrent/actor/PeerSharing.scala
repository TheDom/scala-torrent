package com.dominikgruber.scalatorrent.actor

import akka.actor.ActorRef
import akka.io.Tcp._
import com.dominikgruber.scalatorrent.actor.PeerSharing.SendToPeer
import com.dominikgruber.scalatorrent.actor.ToByteString._
import com.dominikgruber.scalatorrent.actor.Torrent.{AreWeInterested, NextRequest}
import com.dominikgruber.scalatorrent.metainfo.MetaInfo
import com.dominikgruber.scalatorrent.peerwireprotocol.{Message, _}
import com.dominikgruber.scalatorrent.transfer.BitSetUtil

object PeerSharing {
  case class SendToPeer(message: Message)
}

trait PeerSharing {
  actor: PeerActor =>

  def sharing(tcp: ActorRef, metaInfo: MetaInfo, torrent: ActorRef): Receive = Sharing(tcp, metaInfo, torrent).apply

  case class Sharing(tcp: ActorRef, metaInfo: MetaInfo, torrent: ActorRef) {

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

    var bitfield: Vector[Boolean] = Vector.fill(metaInfo.fileInfo.numPieces)(false)

    def apply: Receive = {
      case SendToPeer(msg: Message) => // from Torrent
        log.debug(s"Sending $msg")
        tcp ! Write(msg)
        if(msg.isInstanceOf[Interested])
          amInterested = true

      case Received(data) => // from Tcp
        Message.unmarshal(data.toVector) match {
          case Some(message) =>
            log.debug(s"Received $message")
            handleMessage(torrent, message)
          case None =>
            log.warning(s"Received unknown message: ${Hex(data)}")
            //TODO parse failure
        }

      case PeerClosed => handlePeerClosed // from Tcp
    }

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

  }

}
