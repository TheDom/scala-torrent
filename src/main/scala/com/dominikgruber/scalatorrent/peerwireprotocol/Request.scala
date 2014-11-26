package com.dominikgruber.scalatorrent.peerwireprotocol

import akka.util.ByteStringBuilder

/**
 * request: <len=0013><id=6><index><begin><length>
 * The request message is fixed length, and is used to request a block. The
 * payload contains the following information:
 *
 * - index: integer specifying the zero-based piece index
 * - begin: integer specifying the zero-based byte offset within the piece
 * - length: integer specifying the requested length.
 */
case class Request(index: Int, begin: Int, length: Int) extends Message {
  override def lengthPrefix = 13
  override def messageId = Some(6)

  override def marshal: Vector[Byte] = {
    val bsb = new ByteStringBuilder()
    bsb.putBytes(super.marshal.toArray)
    bsb.putInts(Array(index, begin, length))
    bsb.result().toVector
  }
}