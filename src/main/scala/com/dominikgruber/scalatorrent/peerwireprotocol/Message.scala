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
    else message(4) match {
      case Choke.MESSAGE_ID => Choke.unmarshal(message)
      case Unchoke.MESSAGE_ID => Unchoke.unmarshal(message)
      case Interested.MESSAGE_ID => Interested.unmarshal(message)
      case NotInterested.MESSAGE_ID => NotInterested.unmarshal(message)
      case Have.MESSAGE_ID => Have.unmarshal(message)
      case Bitfield.MESSAGE_ID => Bitfield.unmarshal(message)
      case Request.MESSAGE_ID => Request.unmarshal(message)
      case Piece.MESSAGE_ID => Piece.unmarshal(message)
      case Cancel.MESSAGE_ID => Cancel.unmarshal(message)
      case _ => None
    }
  }
}