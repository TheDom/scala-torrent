package com.dominikgruber.scalatorrent.peerwireprotocol

import akka.util.ByteStringBuilder
import java.nio.ByteBuffer

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
  override def messageId = Some(Request.MESSAGE_ID)

  override def marshal: Vector[Byte] = {
    val bsb = new ByteStringBuilder()
    bsb.putBytes(super.marshal.toArray)
    bsb.putInts(Array(index, begin, length))
    bsb.result().toVector
  }
}

object Request {

  val MESSAGE_ID: Byte = 6

  def unmarshal(message: Vector[Byte]): Option[Request] = {
    if (message.length == 17 && message(4) == MESSAGE_ID && message.slice(0, 4) == Vector[Byte](0, 0, 0, 13)) {
      val index = ByteBuffer.wrap(message.slice(5, 9).toArray).getInt
      val begin = ByteBuffer.wrap(message.slice(9, 13).toArray).getInt
      val length = ByteBuffer.wrap(message.slice(13, 17).toArray).getInt
      Some(Request(index, begin, length))
    }
    else None
  }
}