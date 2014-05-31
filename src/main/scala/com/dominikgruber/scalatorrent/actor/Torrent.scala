package com.dominikgruber.scalatorrent.actor

import akka.actor.{Actor, Props}
import com.dominikgruber.scalatorrent.metainfo.Metainfo
import com.dominikgruber.scalatorrent.tracker.{TrackerResponseWithFailure, TrackerResponseWithSuccess}

object Torrent {
  case class Start()
  case class Shutdown()
}

class Torrent(name: String, metainfo: Metainfo, peerId: String) extends Actor {
  import Torrent._
  import Tracker._

  val tracker = context.actorOf(Props(classOf[Tracker], metainfo, peerId), "tracker")
  tracker ! SendEventStarted

  def receive = started

  def started: Receive = {
    case TrackerResponseReceived(res) => {
      res match {
        case s: TrackerResponseWithSuccess =>
          println(s"[$name] Request to Tracker successful: $res")
          // TODO: Spawn new peer actors
          context become sharing
        case f: TrackerResponseWithFailure =>
          println(s"[$name] Request to Tracker failed: ${f.reason}")
          // TODO: Handle failure
      }
    }
    case TrackerConnectionFailed(msg) => {
      println(s"[$name] Connection to Tracker failed: $msg")
      // TODO: Handle failure
    }
  }

  def sharing: Receive = {
    case Shutdown =>
  }
}
