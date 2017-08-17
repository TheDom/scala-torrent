package com.dominikgruber.scalatorrent.actor

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.dominikgruber.scalatorrent.actor.Coordinator.CreatePeerConnection
import com.dominikgruber.scalatorrent.actor.PeerSharing.SendToPeer
import com.dominikgruber.scalatorrent.actor.Torrent.{AreWeInterested, BlockSize, NextRequest, ReceivedPiece}
import com.dominikgruber.scalatorrent.actor.Tracker.{SendEventStarted, TrackerConnectionFailed, TrackerResponseReceived}
import com.dominikgruber.scalatorrent.metainfo.MetaInfo
import com.dominikgruber.scalatorrent.peerwireprotocol.{Interested, Piece}
import com.dominikgruber.scalatorrent.tracker.{TrackerResponseWithFailure, TrackerResponseWithSuccess}
import com.dominikgruber.scalatorrent.transfer.TransferStatus

import scala.collection.BitSet

object Torrent {
  /**
    * 16kB is standard. Sending a request with more might result in the peer dropping the connection
    * https://wiki.theory.org/index.php/BitTorrentSpecification#Info_in_Single_File_Mode
    */
  val BlockSize: Int = 16 * 1024
  val MaxActivePieces: Int = 1
  case class AreWeInterested(partsAvailable: BitSet)
  case class NextRequest(partsAvailable: BitSet)
  case class ReceivedPiece(piece: Piece, partsAvailable: BitSet)
}

class Torrent(name: String, metaInfo: MetaInfo, peerId: String, coordinator: ActorRef, portIn: Int)
  extends Actor with ActorLogging {

  // Connect to Tracker
  val tracker: ActorRef = {
    val props = Props(classOf[Tracker], metaInfo, peerId, portIn)
    context.actorOf(props, "tracker")
  }

  override def preStart(): Unit = {
    tracker ! SendEventStarted(0, 0)
  }

  override def receive: Receive = findingPeers

  def findingPeers: Receive = {
    case TrackerResponseReceived(res) => res match { // from Tracker
      case s: TrackerResponseWithSuccess =>
        log.debug(s"[$name] Request to Tracker successful: $res")
        s.peers.foreach(peer => {
          coordinator ! CreatePeerConnection(peer, metaInfo)
        })
        context become sharing
      case f: TrackerResponseWithFailure =>
        log.debug(s"[$name] Request to Tracker failed: ${f.reason}")
        // TODO: Handle failure
    }

    case TrackerConnectionFailed(msg) =>
      log.debug(s"[$name] Connection to Tracker failed: $msg")
      // TODO: Handle failure
  }

  val transferStatus = TransferStatus(metaInfo)

  def sharing: Receive = {
    case AreWeInterested(piecesAvailable) => // from PeerConnection
      if(transferStatus.isAnyPieceNew(piecesAvailable))
        sender ! SendToPeer(Interested())
    case NextRequest(piecesAvailable) => // from PeerConnection
      requestNewBlock(piecesAvailable, sender) //TODO begin with 5 requests
    case ReceivedPiece(piece, piecesAvailable) => // from PeerConnection
      //TODO validate numbers received
      //TODO persist data
      transferStatus.markBlockAsCompleted(piece.index, piece.begin/BlockSize)
      requestNewBlock(piecesAvailable, sender)
  }

  def requestNewBlock(piecesAvailable: BitSet, peerConnection: ActorRef): Unit =
    transferStatus.pickNewBlock(piecesAvailable) match {
      case Some(request) =>
        peerConnection ! SendToPeer(request)
      case None => //TODO fully downloaded
    }

}
