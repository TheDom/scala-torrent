package com.dominikgruber.scalatorrent.actor

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{ImplicitSender, TestKit}
import com.dominikgruber.scalatorrent.actor.Coordinator.CreatePeerConnection
import com.dominikgruber.scalatorrent.actor.PeerConnection.Send
import com.dominikgruber.scalatorrent.actor.Torrent.{AreWeInterested, BlockSize, NextRequest, ReceivedPiece}
import com.dominikgruber.scalatorrent.actor.Tracker.{SendEventStarted, TrackerResponseReceived}
import com.dominikgruber.scalatorrent.metainfo.MetaInfo
import com.dominikgruber.scalatorrent.peerwireprotocol.{Interested, Piece, Request}
import com.dominikgruber.scalatorrent.tracker.{Peer, TrackerResponseWithSuccess}
import com.dominikgruber.scalatorrent.util.Stub
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

import scala.collection.BitSet

class TorrentSpec extends TestKit(ActorSystem())
  with ImplicitSender with WordSpecLike with Matchers with BeforeAndAfterAll with MockitoSugar {

  "A Torrent actor" must {

    val meta: MetaInfo = Stub.metaInfo(
      totalLength = 6 * BlockSize,
      pieceLength = 2 * BlockSize)
    val torrent: ActorRef = {
      def createActor = new Torrent("", meta, "", testActor, 0) {
        override val tracker: ActorRef = testActor
      }
      system.actorOf(Props(createActor), "torrent")
    }
    lazy val totalBlocks = meta.fileInfo.totalBytes/BlockSize

    "sanity check" in {
      // next tests depend on this
      totalBlocks shouldBe 6
      meta.fileInfo.numPieces shouldBe 3
    }

    "say hi to tracker" in {
      //after creating the actor
      expectMsg(SendEventStarted(0, 0))
    }

    "create peer connections" in {
      val peer1 = mock[Peer]
      val peer2 = mock[Peer]
      torrent ! TrackerResponseReceived {
        TrackerResponseWithSuccess(0, None, None, 0, 0, List(peer1, peer2), None)
      }
      expectMsg(CreatePeerConnection(peer1, meta))
      expectMsg(CreatePeerConnection(peer2, meta))
    }

    "send Interested when a peer has new pieces" in {
      torrent ! AreWeInterested(BitSet(0))
      expectMsgPF() {
        case Send(Interested()) =>
      }
    }

    "request new blocks in response to MoreRequests" in {
      torrent ! NextRequest(allAvailable)
      ObservedRequests.expectRequest //TODO x5
    }

    "request a new block after receiving ReceivedPiece, and it should be from the same piece" in {
      val firstRequest = {
        ObservedRequests.received.size shouldBe 1
        ObservedRequests.received.head
      }
      val piece = Piece(firstRequest.index, firstRequest.begin, Vector.empty)
      torrent ! ReceivedPiece(piece, allAvailable)
      ObservedRequests.expectRequest
    }

    "the second request should be another block from the same piece" in {
      val (firstRequest, secondRequest) = {
        ObservedRequests.received.size shouldBe 2
        (ObservedRequests.received.head, ObservedRequests.received(1))
      }
      secondRequest.index shouldBe firstRequest.index
      secondRequest.begin should not be firstRequest.begin
    }

    "not send Interested when a peer hasn't got any new pieces" in {
      val secondRequest = {
        ObservedRequests.received.size shouldBe 2
        ObservedRequests.received(1)
      }
      val piece = Piece(secondRequest.index, secondRequest.begin, Vector.empty)
      torrent ! ReceivedPiece(piece, allAvailable)
      ObservedRequests.expectRequest

      val pieceWeAlreadyHave = secondRequest.index
      torrent ! AreWeInterested(BitSet(pieceWeAlreadyHave))
      expectNoMsg()
    }

    object ObservedRequests {
      var received = Seq.empty[Request]

      def expectRequest: Unit = {
        val request = expectMsgPF() {
          case Send(r: Request) => r
        }
        ObservedRequests.received = ObservedRequests.received :+ request
      }
    }

    lazy val allAvailable = BitSet(0, 1, 2)
  }

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

}