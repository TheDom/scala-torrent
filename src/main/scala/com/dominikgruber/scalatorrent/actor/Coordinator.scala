package com.dominikgruber.scalatorrent.actor

import akka.actor.{Props, Actor}
import com.dominikgruber.scalatorrent.metainfo.Metainfo
import util.Random

object Coordinator {
  case class AddTorrentFile(file: String)

  case class TorrentAddedSuccessfully(file: String)
  case class TorrentFileInvalid(file: String, message: String)
}

class Coordinator extends Actor {
  import Coordinator._

  /**
   * 20-byte string used as a unique ID for the client.
   * Azureus-style: '-', two characters for client id, four ascii digits for
   * version number, '-', followed by random numbers.
   * 'SC' was chosen for the client id since 'ST' was already taken.
   *
   * @todo Generate once per client and persist
   */
  lazy val peerId = "-SC0001-" + (100000 + Random.nextInt(899999)) + (100000 + Random.nextInt(899999))

  def receive = {
    case AddTorrentFile(file) =>
      try {
        val name = file.split('/').last.replace(".torrent", "") // TODO: Make sure name is unique
        val metainfo = Metainfo.loadFromFile(file)
        context.actorOf(Props(classOf[Torrent], name, metainfo, peerId), "torrent-" + name)
        sender ! TorrentAddedSuccessfully(file)
      } catch {
        case e: Exception => sender ! TorrentFileInvalid(file, e.getMessage)
      }
  }
}