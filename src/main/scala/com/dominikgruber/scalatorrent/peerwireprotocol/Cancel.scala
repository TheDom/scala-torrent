package com.dominikgruber.scalatorrent.peerwireprotocol

import akka.util.ByteStringBuilder

/**
 * cancel: <len=0013><id=8><index><begin><length>
 * The cancel message is fixed length, and is used to cancel block requests.
 * The payload is identical to that of the "request" message.
 */
case class Cancel(index: Int, begin: Int, length: Int) extends Message {
  override def lengthPrefix = 13
  override def messageId = Some(8)

  override def marshal: Vector[Byte] = {
    val bsb = new ByteStringBuilder()
    bsb.putBytes(super.marshal.toArray)
    bsb.putInts(Array(index, begin, length))
    bsb.result().toVector
  }
}