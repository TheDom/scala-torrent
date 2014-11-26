package com.dominikgruber.scalatorrent.peerwireprotocol

import akka.util.ByteStringBuilder

/**
 * have: <len=0005><id=4><piece index>
 * The have message is fixed length. The payload is the zero-based index of a
 * piece that has just been successfully downloaded and verified via the hash.
 */
case class Have(pieceIndex: Int) extends Message {
  override def lengthPrefix = 5
  override def messageId = Some(4)

  override def marshal: Vector[Byte] = {
    val bsb = new ByteStringBuilder()
    bsb.putBytes(super.marshal.toArray)
    bsb.putInt(pieceIndex)
    bsb.result().toVector
  }
}