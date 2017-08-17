package com.dominikgruber.scalatorrent.actor

import akka.actor.ActorRef
import akka.io.Tcp._
import com.dominikgruber.scalatorrent.actor.Coordinator.{IdentifyTorrent, TorrentInfo}
import com.dominikgruber.scalatorrent.actor.PeerHandshaking.{BeginConnection, ReceiveConnection}
import com.dominikgruber.scalatorrent.actor.ToByteString._
import com.dominikgruber.scalatorrent.metainfo.MetaInfo
import com.dominikgruber.scalatorrent.peerwireprotocol.Handshake

object PeerHandshaking {
  case class BeginConnection(torrent: ActorRef, metaInfo: MetaInfo)
  case class ReceiveConnection(tcp: ActorRef)
}

trait PeerHandshaking {
  peerConnection: PeerActor =>

  def handeshaking: Receive = {
    case BeginConnection(torrent, metaInfo) => // from Torrent
      val tcp = getTcpManager
      tcp ! Connect(remoteAddress)
      context become Begin(tcp, metaInfo, torrent).apply
    case ReceiveConnection(tcp) => // from Coordinator
      context become Respond(tcp).apply
  }

  case class Begin(tcp: ActorRef, metaInfo: MetaInfo, torrent: ActorRef) {
    def apply: Receive = waitTcpConnection

    private def waitTcpConnection: Receive = {
      case Connected(_, _) => // from Tcp
        sender ! Register(self)
        sendHandshake(sender, metaInfo)
        context become waitSecondHandshake

      case CommandFailed(_: Connect) => // from Tcp
        log.error("Failed to open TCP connection")
        //TODO: Handle failure
    }

    private def waitSecondHandshake: Receive = {
      case Received(data) => // from Tcp
        Handshake.unmarshall(data.toVector) match {
          case Some(h: Handshake) =>
            log.debug(s"Received 2nd handshake: $h")
            //TODO validate handshake
            context become sharing(tcp, metaInfo, torrent)
          case None =>
            log.warning(s"Failed to parse 2nd handshake: ${Hex(data)}")
            //TODO drop peer
        }
      case PeerClosed => handlePeerClosed // from Tcp
    }

  }

  case class Respond(tcp: ActorRef) {
    def apply: Receive = waitFirstHandshake

    private def waitFirstHandshake: Receive = {
      case Received(data) => // from Tcp
        Handshake.unmarshall(data.toVector) match {
          case Some(h: Handshake) =>
            log.debug(s"Received 1st handshake: $h")
            coordinator ! IdentifyTorrent(h.infoHashString)
            context become waitTorrentInfo(h)
          case None =>
            log.warning(s"Failed to parse 1st handshake: ${Hex(data)}")
            //TODO drop peer
        }
      case PeerClosed => handlePeerClosed // from Tcp
    }

    private def waitTorrentInfo(h: Handshake): Receive = {
      case TorrentInfo(metaInfo, torrent) => // from Coordinator
        sendHandshake(tcp, metaInfo)
        context become sharing(tcp, metaInfo, torrent)
    }
  }

  private def sendHandshake(tcp: ActorRef, metaInfo: MetaInfo) = {
    val handshake = Handshake(metaInfo.fileInfo.infoHash, selfPeerId)
    tcp ! Write(handshake)
  }

}

