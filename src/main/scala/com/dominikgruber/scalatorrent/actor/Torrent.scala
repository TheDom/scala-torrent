package com.dominikgruber.scalatorrent.actor

import akka.actor.{Actor, ActorRef, Props}
import com.dominikgruber.scalatorrent.actor.Coordinator.{CreatePeerConnection, PeerConnected}
import com.dominikgruber.scalatorrent.metainfo.MetaInfo
import com.dominikgruber.scalatorrent.tracker.{Peer, TrackerResponseWithFailure, TrackerResponseWithSuccess}

import scala.collection.mutable

class Torrent(name: String, metaInfo: MetaInfo, peerId: String, connectionHandler: ActorRef, portIn: Int) extends Actor {
  import PeerConnection._
  import Tracker._

  val knownPeers = mutable.Set.empty[Peer]
  var peerConnections = mutable.Set.empty[ActorRef]

  // Connect to Tracker
  val tracker = context.actorOf(Props(classOf[Tracker], metaInfo, peerId, portIn), "tracker")
  tracker ! SendEventStarted(0, 0)

  override def receive = started

  def started: Receive = {
    case TrackerResponseReceived(res) => res match { // from Tracker
      case s: TrackerResponseWithSuccess =>
        println(s"[$name] Request to Tracker successful: $res")
        s.peers.foreach(peer => {
          knownPeers.add(peer)
          connectionHandler ! CreatePeerConnection(peer, metaInfo)
        })
        context become sharing
      case f: TrackerResponseWithFailure =>
        println(s"[$name] Request to Tracker failed: ${f.reason}")
        // TODO: Handle failure
    }

    case TrackerConnectionFailed(msg) =>
      println(s"[$name] Connection to Tracker failed: $msg")
      // TODO: Handle failure
  }

  def sharing: Receive = {
    case PeerConnected(peerConnection) => // from PeerConnection
      peerConnections.add(peerConnection)

    // TODO: Protocol

  }
}
