package com.dominikgruber.scalatorrent.peerwireprotocol

/**
 * not interested: <len=0001><id=3>
 * The not interested message is fixed-length and has no payload.
 */
case class NotInterested() extends Message {
  override def lengthPrefix = 1
  override def messageId = Some(3)
}