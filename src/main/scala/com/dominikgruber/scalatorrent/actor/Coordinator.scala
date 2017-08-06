package com.dominikgruber.scalatorrent.actor

import java.io.File
import java.net.InetSocketAddress

import akka.actor.{Actor, ActorRef, Props}
import akka.io.Tcp.{Bind, CommandFailed, Connected, Register}
import akka.io.{IO, Tcp}
import com.dominikgruber.scalatorrent.actor.Coordinator._
import com.dominikgruber.scalatorrent.actor.PeerConnection.{BeginConnection, ReceiveConnection}
import com.dominikgruber.scalatorrent.metainfo.MetaInfo
import com.dominikgruber.scalatorrent.tracker.Peer
import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.mutable
import scala.util.Random

object Coordinator {
  case class AddTorrentFile(file: String)
  case class TorrentAddedSuccessfully(file: String)
  case class TorrentFileInvalid(file: String, message: String)
  case class IdentifyTorrent(infoHash: String)
  case class CreatePeerConnection(peer: Peer, metaInfo: MetaInfo)
  case class TorrentInfo(metaInfo: MetaInfo, torrent: ActorRef)
}

class Coordinator extends Actor {

  import context.system

  /**
   * 20-byte string used as a unique ID for the client.
   * Azureus-style: '-', two characters for client id, four ascii digits for
   * version number, '-', followed by random numbers.
   * 'SC' was chosen for the client id since 'ST' was already taken.
   *
   * @todo Generate once and persist
   */
  lazy val peerId: String = {
    def rand = 100000 + Random.nextInt(899999)
    s"-SC0001-$rand$rand"
  }

  val conf: Config = ConfigFactory.load.getConfig("scala-torrent")
  val torrents = mutable.Map.empty[String,(ActorRef, MetaInfo)]

  // Start actor to handle incoming connections
  val portIn: Int = conf.getInt("port")
  val endpoint = new InetSocketAddress("localhost", portIn)

  // Start listening to incoming connections
  IO(Tcp) ! Tcp.Bind(self, endpoint)

  override def receive: Receive = {
    case AddTorrentFile(file) => // from Boot
      addTorrentFile(file)

    case CreatePeerConnection(peer, metaInfo) => // from Torrent
      val peerConnection = createPeerConnectionActor(peer.inetSocketAddress)
      peerConnection ! BeginConnection(sender, metaInfo)

    case IdentifyTorrent(infoHash) => // from PeerConnection
      torrents.get(infoHash) match {
        case Some((torrent, metaInfo)) =>
          sender ! TorrentInfo(metaInfo, torrent)
        case None => //TODO handle not found
      }

    case Connected(remoteAddress, _) => // from Tcp
      val peerConnection = createPeerConnectionActor(remoteAddress)
      peerConnection ! ReceiveConnection(sender)
      sender ! Register(peerConnection)

    case CommandFailed(_: Bind) => // from Tcp
      // TODO: Handle failure
  }

  private def addTorrentFile(file: String): Unit = {
    try {
      val name = file.split('/').last.replace(".torrent", "")
      val metaInfo = MetaInfo(new File(file))
      val torrentProps = Props(classOf[Torrent], name, metaInfo, peerId, self, portIn)
      val torrentActor = context.actorOf(torrentProps, "torrent-" + metaInfo.fileInfo.infoHashString)
      torrents(metaInfo.fileInfo.infoHashString) = (torrentActor, metaInfo)
      sender ! TorrentAddedSuccessfully(file)
    } catch {
      case e: Exception => sender ! TorrentFileInvalid(file, e.getMessage)
    }
  }

  private def createPeerConnectionActor(remoteAddress: InetSocketAddress): ActorRef = {
    val addressStr = remoteAddress.toString.replace("/", "")
    val name = s"peer-connection-$addressStr"
    context.actorOf(Props(classOf[PeerConnection], remoteAddress, peerId, self), name)
  }
}