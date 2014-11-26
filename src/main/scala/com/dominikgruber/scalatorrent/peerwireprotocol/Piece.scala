package com.dominikgruber.scalatorrent.peerwireprotocol

import akka.util.ByteStringBuilder
import java.nio.ByteBuffer

/**
 * piece: <len=0009+X><id=7><index><begin><block>
 * The piece message is variable length, where X is the length of the block. The
 * payload contains the following information:
 *
 * - index: integer specifying the zero-based piece index
 * - begin: integer specifying the zero-based byte offset within the piece
 * - block: block of data, which is a subset of the piece specified by index.
 */
case class Piece(index: Int, begin: Int, block: Vector[Byte]) extends Message {
  override def lengthPrefix = 9 + block.length
  override def messageId = Some(Piece.MESSAGE_ID)

  override def marshal: Vector[Byte] = {
    val bsb = new ByteStringBuilder()
    bsb.putBytes(super.marshal.toArray)
    bsb.putInts(Array(index, begin))
    bsb.putBytes(block.toArray)
    bsb.result().toVector
  }
}

object Piece {

  val MESSAGE_ID: Byte = 7

  def unmarshal(message: Vector[Byte]): Option[Piece] = {
    if (message.length > 14 && message(4) == MESSAGE_ID) {
      val l = ByteBuffer.wrap(message.slice(0, 4).toArray).getInt - 9
      if (l + 13 == message.length) {
        val index = ByteBuffer.wrap(message.slice(5, 9).toArray).getInt
        val begin = ByteBuffer.wrap(message.slice(9, 13).toArray).getInt
        return Some(Piece(index, begin, message.drop(13)))
      }
    }
    None
  }
}