package com.dominikgruber.scalatorrent.peerwireprotocol

import akka.util.ByteStringBuilder
import java.nio.ByteOrder

/**
 * All of the messages besides the Handshake take the form of
 * <length prefix><message ID><payload>. The length prefix is a four byte
 * big-endian value. The message ID is a single decimal byte. The payload is
 * message dependent.
 */
abstract class Message {
  def lengthPrefix: Int
  def messageId: Option[Byte]

  protected implicit val byteOrder = ByteOrder.BIG_ENDIAN

  def marshal: Vector[Byte] = {
    val bsb = new ByteStringBuilder()
    bsb.putInt(lengthPrefix)
    if (messageId.isDefined) bsb.putByte(messageId.get)
    bsb.result().toVector
  }
}

object Message {

  def unmarshal(message: Vector[Byte]): Option[Message] = {
    if (message.length < 4) None
    else if (message.length == 4) KeepAlive.unmarshal(message)
    else {
      val messageId = message(4)
      if (messageId == Choke.MESSAGE_ID) Choke.unmarshal(message)
      else if (messageId == Unchoke.MESSAGE_ID) Unchoke.unmarshal(message)
      else if (messageId == Interested.MESSAGE_ID) Interested.unmarshal(message)
      else if (messageId == NotInterested.MESSAGE_ID) NotInterested.unmarshal(message)
      else if (messageId == Have.MESSAGE_ID) Have.unmarshal(message)
      else if (messageId == Bitfield.MESSAGE_ID) Bitfield.unmarshal(message)
      else if (messageId == Request.MESSAGE_ID) Request.unmarshal(message)
      else if (messageId == Piece.MESSAGE_ID) Piece.unmarshal(message)
      else if (messageId == Cancel.MESSAGE_ID) Cancel.unmarshal(message)
      else None
    }
  }
}