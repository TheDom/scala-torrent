package com.dominikgruber.scalatorrent.actor

import akka.actor.{ActorRef, Props, Actor}
import com.dominikgruber.scalatorrent.metainfo.Metainfo
import com.dominikgruber.scalatorrent.peerwireprotocol.Handshake
import com.typesafe.config.ConfigFactory
import java.io.File
import java.net.InetSocketAddress
import scala.collection.mutable
import util.Random

object Coordinator {
  case class AddTorrentFile(file: String)
  case class TorrentAddedSuccessfully(file: String)
  case class TorrentFileInvalid(file: String, message: String)

  case class IncomingPeerConnection(peerConnection: ActorRef, handshake: Handshake)
}

class Coordinator extends Actor {
  import Coordinator._
  import Torrent.{IncomingPeerConnection => TorrentIncomingPeerConnection}

  /**
   * 20-byte string used as a unique ID for the client.
   * Azureus-style: '-', two characters for client id, four ascii digits for
   * version number, '-', followed by random numbers.
   * 'SC' was chosen for the client id since 'ST' was already taken.
   *
   * @todo Generate once and persist
   */
  lazy val peerId = "-SC0001-" + (100000 + Random.nextInt(899999)) + (100000 + Random.nextInt(899999))

  val conf = ConfigFactory.load.getConfig("scala-torrent")
  val torrentActors = mutable.Map.empty[String,ActorRef]
  val torrentMetainfos = mutable.Map.empty[String,Metainfo]

  // Start actor to handle incoming connections
  val portIn = conf.getInt("port")
  val endpoint = new InetSocketAddress("localhost", portIn)
  val connectionHandler = context.actorOf(Props(classOf[ConnectionHandler], endpoint, peerId), "connection-handler")

  def receive = {
    case AddTorrentFile(file) =>
      try {
        val name = file.split('/').last.replace(".torrent", "")
        val metainfo = Metainfo(new File(file))
        val torrent = context.actorOf(Props(classOf[Torrent], name, metainfo, peerId, connectionHandler, portIn), "torrent-" + metainfo.info.infoHashString)
        torrentActors(metainfo.info.infoHashString) = torrent
        torrentMetainfos(metainfo.info.infoHashString) = metainfo
        sender ! TorrentAddedSuccessfully(file)
      } catch {
        case e: Exception => sender ! TorrentFileInvalid(file, e.getMessage)
      }

    case IncomingPeerConnection(peerConnection, handshake) if torrentActors.isDefinedAt(handshake.infoHashString) =>
      torrentActors(handshake.infoHashString) ! TorrentIncomingPeerConnection(peerConnection)
  }
}