package com.dominikgruber.scalatorrent.actor

import java.net.InetSocketAddress

import akka.actor.{ActorRef, Props}
import akka.io.Tcp._
import com.dominikgruber.scalatorrent.actor.Coordinator.{IdentifyTorrent, TorrentInfo}
import com.dominikgruber.scalatorrent.actor.PeerHandshaking.{BeginConnection, ReceiveConnection}
import com.dominikgruber.scalatorrent.actor.PeerSharing.SendToPeer
import com.dominikgruber.scalatorrent.actor.ToByteString._
import com.dominikgruber.scalatorrent.actor.Torrent.{AreWeInterested, NextRequest}
import com.dominikgruber.scalatorrent.peerwireprotocol._
import com.dominikgruber.scalatorrent.util.{ActorSpec, Mocks}

class PeerActorSpec extends ActorSpec {

  "A PeerConnection actor in Handshake-Begin behavior" must {
    val peerConnection = startActor("Handshake-Begin")

    "connect to Tcp" in {
      peerConnection ! BeginConnection(testActor, meta)
      expectMsg(Connect(address))
    }

    "send the 1st Handshake when Connected (Tcp)" in {
      peerConnection ! Connected(mock[InetSocketAddress], address)
      expectMsgType[Register]
      expectMsgType[Write] //TODO check it's a handshake
    }

    "become Connected (Torrent) after receiving the 2nd Handshake" in {
      peerConnection ! Received(handshake)
      mustBeConnected(peerConnection)
    }

  }

  "A PeerConnection actor in Handshake-Receive behavior" must {
    val peerConnection = startActor("Handshake-Receive")

    "receive the 1st handshake then identify" in {
      peerConnection ! ReceiveConnection(testActor)
      peerConnection ! Received(handshake)
      expectMsgType[IdentifyTorrent]
    }

    "receive identification then send the 2nd handshake" in {
      peerConnection ! TorrentInfo(meta, testActor)
      expectMsgType[Write] //TODO check it's a handshake
    }

    "be connected after the 2nd handshake" in {
      mustBeConnected(peerConnection)
    }

  }

  "A PeerConnection actor in Connected behavior" must {
    val peerConnection = startActor("Connected")

    "skip to connected behavior" in {
      //bring Actor to the desired state, the real tests follow
      skipToConnected(peerConnection)
    }

    "ask AreWeInterested after receiving Bitfield" in {
      peerConnection ! Received(bitfield)
      expectMsgType[AreWeInterested]
    }

    "ask AreWeInterested after receiving Have" in {
      peerConnection ! Received(haveMsg)
      expectMsgType[AreWeInterested]
    }

    "ask NextRequest after receiving Unchoke" in {
      peerConnection ! Received(unchoke)
      expectMsgType[NextRequest]
    }

    "forward Pieces" in {
      peerConnection ! Received(piece)
      expectMsgType[Piece]
    }

    "send messages to Tcp" in {
      peerConnection ! SendToPeer(Interested())
      expectMsgType[Write] //TODO check it's the same
    }

  }

  private def skipToConnected(peerConnection: ActorRef) = {
    peerConnection ! ReceiveConnection(testActor)
    peerConnection ! Received(handshake)
    expectMsgType[IdentifyTorrent]
    peerConnection ! TorrentInfo(meta, testActor)
    expectMsgType[Write]
  }

  private def mustBeConnected(peerConnection: ActorRef): Unit = {
    peerConnection ! Received(bitfield)
    expectMsgType[AreWeInterested]
  }

  lazy val address = new InetSocketAddress("dummy", 123)
  lazy val meta = Mocks.metaInfo()
  lazy val handshake = {
    val peerId = "peer-id-has-20-chars"
    Handshake(Mocks.infoHash, peerId)
  }
  lazy val bitfield = Bitfield(Vector(true))
  lazy val haveMsg = Have(0)
  lazy val unchoke = Unchoke()
  lazy val piece = Piece(0, 0, Vector(0, 0))
  def startActor(name: String): ActorRef = {
    def createActor = new PeerActor(address, "", testActor) {
      override val getTcpManager: ActorRef = testActor
    }
    system.actorOf(Props(createActor), name)
  }

}
