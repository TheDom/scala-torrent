package com.dominikgruber.scalatorrent.peerwireprotocol

/**
 * unchoke: <len=0001><id=1>
 * The unchoke message is fixed-length and has no payload.
 */
case class Unchoke() extends Message {
  override def lengthPrefix = 1
  override def messageId = Some(1)
}