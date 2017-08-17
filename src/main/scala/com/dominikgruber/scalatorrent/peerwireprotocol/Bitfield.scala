package com.dominikgruber.scalatorrent.peerwireprotocol

import java.nio.ByteBuffer

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
  override def messageId = Some(Bitfield.MESSAGE_ID)

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

  override def toString: String = s"BitField(...)"
}

object Bitfield {

  val MESSAGE_ID: Byte = 5

  def unmarshal(message: Vector[Byte]): Option[Bitfield] = {
    if (message.length > 5 && message(4) == MESSAGE_ID) {
      val l = ByteBuffer.wrap(message.slice(0, 4).toArray).getInt - 1
      if (l + 5 == message.length) {
        val downloadedPieces = message.drop(5).foldLeft((new Array[Boolean](l * 8), 0))((z, b) => {
          var (a, i) = z

          for (j <- 7 to 0 by -1) {
            a(i) = (b & (1 << j)) > 0
            i += 1
          }

          (a, i)
        })._1.toVector
        return Some(Bitfield(downloadedPieces))
      }
    }
    None
  }
}