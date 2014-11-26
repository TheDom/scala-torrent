package com.dominikgruber.scalatorrent.peerwireprotocol

/**
 * interested: <len=0001><id=2>
 * The interested message is fixed-length and has no payload.
 */
case class Interested() extends Message {
  override def lengthPrefix = 1
  override def messageId = Some(2)
}