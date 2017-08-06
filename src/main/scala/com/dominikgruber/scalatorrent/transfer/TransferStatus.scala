package com.dominikgruber.scalatorrent.transfer

import com.dominikgruber.scalatorrent.actor.Torrent.BlockSize
import com.dominikgruber.scalatorrent.metainfo.MetaInfo
import com.dominikgruber.scalatorrent.peerwireprotocol.Request
import com.dominikgruber.scalatorrent.transfer.PickRandom._

import scala.collection.{BitSet, mutable}

case class TransferStatus(metaInfo: MetaInfo) {

  val totalPieces: Int = metaInfo.fileInfo.numPieces
  val blocksPerPiece: Int = metaInfo.fileInfo.pieceLength / BlockSize

  type Flags = mutable.Seq[Boolean]

  /**
    * Marks which pieces from 0 to [[totalPieces]] we have completed
    */
  private val pieceStatus: Flags =
    mutable.Seq.fill(totalPieces)(false)

  /**
    * Marks which blocks we have from each piece being actively downloaded
    */
  private val blockStatus: mutable.Map[Int, Flags] =
    mutable.Map.empty

  def markBlockAsCompleted(piece: Int, block: Int): Unit = {
    for {
      blocks <- activeBlocks(piece)
    } yield {
      blocks(block) = true
      if(blocks.forall(_ == true)) {
        pieceStatus(piece) = true
        blockStatus -= piece
      }
    }
  }

  /**
    * @param available in the remote peer
    * @return whether they have any block that we're missing
    */
  def isAnyPieceNew(available: BitSet): Boolean =
    newPieces(available) nonEmpty

  private def newPieces(available: BitSet) = {
    val alreadyHave = BitSetUtil.fromBooleans(pieceStatus)
    available &~ alreadyHave
  }

  /**
    * @param available in the remote peer
    * @return A [[Request]] to get a piece we're missing from a remote peer
    */
  def pickNewBlock(available: BitSet): Option[Request] = {

    def randomActivePiece: Option[Int] = {
      val activePieces = BitSet(blockStatus.keys.toSeq:_*)
      val intersection = activePieces & available
      intersection.toSeq.randomElement
    }

    def randomMissingPiece: Option[Int] =
      newPieces(available).toSeq.randomElement

    def pickPiece: Option[Int] =
      randomActivePiece
        .orElse(randomMissingPiece)

    for {
      piece <- pickPiece
      blocks <- activeBlocks(piece)
      blockIndex <- blocks.randomIndexOf(false)
      blockBegin = blockIndex * BlockSize
    } yield Request(piece, blockBegin, BlockSize)
  }

  private def activeBlocks(piece: Int): Option[Flags] =
    if(pieceStatus(piece)) None
    else {
      def allBlocksPending: Flags = mutable.Seq.fill(blocksPerPiece)(false)
      Some(blockStatus.getOrElseUpdate(piece, allBlocksPending))
    }

}

object BitSetUtil {
  def fromBooleans(bools: Seq[Boolean]): BitSet = {
    val indexesOfTrue = bools.zipWithIndex.collect { case (true, i) => i }
    BitSet(indexesOfTrue: _*)
  }
}