package com.dominikgruber.scalatorrent.peerwireprotocol

/**
 * keep-alive: <len=0000>
 * The keep-alive message is a message with zero bytes, specified with the
 * length prefix set to zero. There is no message ID and no payload. Peers may
 * close a connection if they receive no messages (keep-alive or any other
 * message) for a certain period of time, so a keep-alive message must be sent
 * to maintain the connection alive if no command have been sent for a given
 * amount of time. This amount of time is generally two minutes.
 */
case class KeepAlive() extends Message {
  override def lengthPrefix = 0
  override def messageId = None
}