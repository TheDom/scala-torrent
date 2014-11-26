package com.dominikgruber.scalatorrent.peerwireprotocol

import akka.util.ByteStringBuilder

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
  override def messageId = Some(7)

  override def marshal: Vector[Byte] = {
    val bsb = new ByteStringBuilder()
    bsb.putBytes(super.marshal.toArray)
    bsb.putInts(Array(index, begin))
    bsb.putBytes(block.toArray)
    bsb.result().toVector
  }
}