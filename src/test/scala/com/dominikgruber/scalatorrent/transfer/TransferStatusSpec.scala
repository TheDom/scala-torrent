package com.dominikgruber.scalatorrent.transfer

import com.dominikgruber.scalatorrent.metainfo.MetaInfo
import com.dominikgruber.scalatorrent.util.{Stub, UnitSpec}
import com.dominikgruber.scalatorrent.actor.Torrent.BlockSize
import com.dominikgruber.scalatorrent.peerwireprotocol.Request
import org.scalatest.PrivateMethodTester

import scala.collection.{BitSet, mutable}

class TransferStatusSpec extends UnitSpec with PrivateMethodTester {

  it should "begin with all blocks missing" in {
    val state = TransferStatus(meta)
    state.getPieceStatus shouldBe Seq(false, false, false)
    state.getBlockStatus shouldBe empty
  }

  it should "mark a block" in {
    val state = TransferStatus(meta)
    state.markBlockAsCompleted(1, 1)

    state.getPieceStatus shouldBe Seq(false, false, false)
    state.getBlockStatus shouldBe Map(1 -> Seq(false, true))
  }

  it should "mark a piece" in {
    val state = TransferStatus(meta)
    state.markBlockAsCompleted(1, 0)
    state.markBlockAsCompleted(1, 1)

    state.getPieceStatus shouldBe Seq(false, true, false)
    state.getBlockStatus shouldBe empty
  }

  it should "ignore a redundant mark" in {
    val state = TransferStatus(meta)

    state.markBlockAsCompleted(1, 0)
    state.markBlockAsCompleted(1, 0)
    state.getPieceStatus shouldBe Seq(false, false, false)
    state.getBlockStatus shouldBe Map(1 -> Seq(true, false))

    state.markBlockAsCompleted(1, 1)
    state.markBlockAsCompleted(1, 0)
    state.getPieceStatus shouldBe Seq(false, true, false)
    state.getBlockStatus shouldBe empty
  }

  it should "only pick missing parts" in {
    val state = TransferStatus(meta)
    state.markBlockAsCompleted(0, 0)
    state.markBlockAsCompleted(0, 1)
    state.markBlockAsCompleted(1, 0)
    state.markBlockAsCompleted(1, 1)

    state.pickNewBlock(allAvailable) foreach {
      case Request(piece, block, _) => piece shouldBe 2
    }
  }

  it should "only pick missing blocks" in {
    val state = TransferStatus(meta)
    state.markBlockAsCompleted(0, 0)
    state.markBlockAsCompleted(1, 0)
    state.markBlockAsCompleted(2, 0)

    state.pickNewBlock(allAvailable) foreach {
      case Request(piece, block, _) => block shouldBe 1 * BlockSize
    }
  }

  it should "pick None when complete" in {
    val state = TransferStatus(meta)
    state.markBlockAsCompleted(0, 0)
    state.markBlockAsCompleted(0, 1)
    state.markBlockAsCompleted(1, 0)
    state.markBlockAsCompleted(1, 1)
    state.markBlockAsCompleted(2, 0)
    state.markBlockAsCompleted(2, 1)

    state.pickNewBlock(allAvailable) shouldBe None
  }

  val meta: MetaInfo = Stub.metaInfo(
    totalLength = 6 * BlockSize,
    pieceLength = 2 * BlockSize)
  val allAvailable = BitSet(0, 1, 2)

  type Flags = mutable.Seq[Boolean]
  val pieceStatus = PrivateMethod[Flags]('pieceStatus)
  val blockStatus = PrivateMethod[mutable.Map[Int, Flags]]('blockStatus)
  implicit class WhiteBox(sut: TransferStatus) {
    def getPieceStatus: Flags = sut invokePrivate pieceStatus()
    def getBlockStatus: mutable.Map[Int, Flags] = sut invokePrivate blockStatus()
  }

}
