package com.dominikgruber.scalatorrent.peerwireprotocol

/**
 * choke: <len=0001><id=0>
 * The choke message is fixed-length and has no payload.
 */
case class Choke() extends Message {
  override def lengthPrefix = 1
  override def messageId = Some(0)
}