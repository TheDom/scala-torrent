package com.dominikgruber.scalatorrent.peerwireprotocol

import akka.util.ByteStringBuilder
import java.nio.ByteBuffer

/**
 * cancel: <len=0013><id=8><index><begin><length>
 * The cancel message is fixed length, and is used to cancel block requests.
 * The payload is identical to that of the "request" message.
 */
case class Cancel(index: Int, begin: Int, length: Int) extends Message {
  override def lengthPrefix = 13
  override def messageId = Some(Cancel.MESSAGE_ID)

  override def marshal: Vector[Byte] = {
    val bsb = new ByteStringBuilder()
    bsb.putBytes(super.marshal.toArray)
    bsb.putInts(Array(index, begin, length))
    bsb.result().toVector
  }
}

object Cancel {

  val MESSAGE_ID: Byte = 8

  def unmarshal(message: Vector[Byte]): Option[Cancel] = {
    if (message.length == 17 && message(4) == MESSAGE_ID) {
      val l = ByteBuffer.wrap(message.slice(0, 4).toArray).getInt
      if (l == 13) {
        val index = ByteBuffer.wrap(message.slice(5, 9).toArray).getInt
        val begin = ByteBuffer.wrap(message.slice(9, 13).toArray).getInt
        val length = ByteBuffer.wrap(message.slice(13, 17).toArray).getInt
        return Some(Cancel(index, begin, length))
      }
    }
    None
  }
}