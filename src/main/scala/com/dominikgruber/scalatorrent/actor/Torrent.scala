package com.dominikgruber.scalatorrent.actor

import akka.actor.{ActorRef, Actor, Props}
import com.dominikgruber.scalatorrent.metainfo.Metainfo
import com.dominikgruber.scalatorrent.tracker.{Peer, TrackerResponseWithFailure, TrackerResponseWithSuccess}
import scala.collection.mutable

object Torrent {
  case class Start()
  case class Shutdown()

  case class IncomingPeerConnection(peerConnection: ActorRef)
}

class Torrent(name: String, metainfo: Metainfo, peerId: String, connectionHandler: ActorRef, portIn: Int) extends Actor {
  import Torrent._
  import ConnectionHandler._
  import PeerConnection._
  import Tracker._
  import context.system

  val knownPeers = mutable.Set.empty[Peer]
  var peerConnections = mutable.Set.empty[ActorRef]

  // Connect to Tracker
  val tracker = context.actorOf(Props(classOf[Tracker], metainfo, peerId, portIn), "tracker")
  tracker ! SendEventStarted(0, 0)

  def receive = started

  def started: Receive = {
    case TrackerResponseReceived(res) => res match {
      case s: TrackerResponseWithSuccess =>
        println(s"[$name] Request to Tracker successful: $res")
        s.peers.foreach(peer => {
          knownPeers.add(peer)
          connectionHandler ! CreatePeerConnection(peer)
        })
        context become sharing
      case f: TrackerResponseWithFailure =>
        println(s"[$name] Request to Tracker failed: ${f.reason}")
      // TODO: Handle failure
    }

    case TrackerConnectionFailed(msg) => {
      println(s"[$name] Connection to Tracker failed: $msg")
      // TODO: Handle failure
    }
  }

  def sharing: Receive = {
    case PeerConnectionCreated(peerConnection, peer) =>
      peerConnections.add(peerConnection)
      peerConnection ! AttachToTorrent(self, metainfo)
      peerConnection ! SendHandshake

    case IncomingPeerConnection(peerConnection) =>
      peerConnections.add(peerConnection)
      peerConnection ! AttachToTorrent(self, metainfo)
      peerConnection ! SendHandshake

    // TODO: Protocol

    case Shutdown =>
      // TODO: Notify tracker and close peer connections
  }
}
