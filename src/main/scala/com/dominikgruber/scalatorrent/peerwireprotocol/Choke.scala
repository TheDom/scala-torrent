package com.dominikgruber.scalatorrent.peerwireprotocol

/**
 * choke: <len=0001><id=0>
 * The choke message is fixed-length and has no payload.
 */
case class Choke() extends Message {
  override def lengthPrefix = 1
  override def messageId = Some(Choke.MESSAGE_ID)
}

object Choke {

  val MESSAGE_ID: Byte = 0

  def unmarshal(message: Vector[Byte]): Option[Choke] = {
    if (message == Choke().marshal) Some(Choke())
    else None
  }
}