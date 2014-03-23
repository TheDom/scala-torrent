package com.dominikgruber.scalatorrent.actor

import akka.actor.Actor
import com.dominikgruber.scalatorrent.metainfo.Metainfo

object TorrentCoordinator {
  case class AddTorrentFile(file: String)

  case class TorrentAddedSuccessfully(file: String)
  case class TorrentFileInvalid(file: String, message: String)
}

class TorrentCoordinator extends Actor {
  import TorrentCoordinator._

  def receive = {
    case AddTorrentFile(file) =>
      try {
        val metainfo = Metainfo.loadFromFile(file)
        // TODO: Spawn new actor to start torrent
      } catch {
        case e: Exception => sender ! TorrentFileInvalid(file, e.getMessage)
      }
      sender ! TorrentAddedSuccessfully(file)
  }
}