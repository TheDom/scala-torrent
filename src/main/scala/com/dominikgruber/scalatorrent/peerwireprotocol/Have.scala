package com.dominikgruber.scalatorrent.peerwireprotocol

import akka.util.ByteStringBuilder
import java.nio.ByteBuffer

/**
 * have: <len=0005><id=4><piece index>
 * The have message is fixed length. The payload is the zero-based index of a
 * piece that has just been successfully downloaded and verified via the hash.
 */
case class Have(pieceIndex: Int) extends Message {
  override def lengthPrefix = 5
  override def messageId = Some(Have.MESSAGE_ID)

  override def marshal: Vector[Byte] = {
    val bsb = new ByteStringBuilder()
    bsb.putBytes(super.marshal.toArray)
    bsb.putInt(pieceIndex)
    bsb.result().toVector
  }
}

object Have {

  val MESSAGE_ID: Byte = 4

  def unmarshal(message: Vector[Byte]): Option[Have] = {
    if (message.length == 9 && message(4) == MESSAGE_ID) {
      val l = ByteBuffer.wrap(message.slice(0, 4).toArray).getInt
      if (l == 5) {
        val pieceIndex = ByteBuffer.wrap(message.slice(5, 9).toArray).getInt
        return Some(Have(pieceIndex))
      }
    }
    None
  }
}