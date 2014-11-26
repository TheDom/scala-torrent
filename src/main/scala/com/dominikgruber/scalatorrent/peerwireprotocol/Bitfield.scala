package com.dominikgruber.scalatorrent.peerwireprotocol

import akka.util.ByteStringBuilder

/**
 * bitfield: <len=0001+X><id=5><bitfield>
 * The bitfield message may only be sent immediately after the handshaking
 * sequence is completed, and before any other messages are sent. It is
 * optional, and need not be sent if a client has no pieces.
 *
 * The bitfield message is variable length, where X is the length of the
 * bitfield. The payload is a bitfield representing the pieces that have been
 * successfully downloaded. The high bit in the first byte corresponds to piece
 * index 0. Bits that are cleared indicated a missing piece, and set bits
 * indicate a valid and available piece. Spare bits at the end are set to zero.
 */
case class Bitfield(downloadedPieces: Vector[Boolean]) extends Message {
  override def lengthPrefix = 1 + byteVectorLength(downloadedPieces.length)
  override def messageId = Some(5)

  override def marshal: Vector[Byte] = {
    val bsb = new ByteStringBuilder()
    bsb.putBytes(super.marshal.toArray)
    bsb.putBytes(booleanToByteVector(downloadedPieces).toArray)
    bsb.result().toVector
  }

  private def booleanToByteVector(v: Vector[Boolean]): Vector[Byte] =
    v.foldLeft((new Array[Byte](byteVectorLength(v.length)), 0, 7))((z, b) => {
      val (a, i, s) = z

      if (b) a(i) = (a(i) | (1 << s)).toByte

      if (s == 0) (a, i + 1, 7)
      else        (a, i, s - 1)
    })._1.toVector

  private def byteVectorLength(booleanVectorLength: Int): Int =
    Math.ceil(booleanVectorLength / 8.0).toInt
}