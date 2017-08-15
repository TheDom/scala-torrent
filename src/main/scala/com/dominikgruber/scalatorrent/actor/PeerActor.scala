package com.dominikgruber.scalatorrent.actor

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.io.{IO, Tcp}
import akka.util.ByteString
import com.dominikgruber.scalatorrent.peerwireprotocol.{Message, _}

case class PeerActor(remoteAddress: InetSocketAddress, selfPeerId: String, coordinator: ActorRef)
  extends Actor with ActorLogging with PeerHandshaking with PeerSharing {

  def getTcpManager = IO(Tcp)(context.system)

  override def receive: Receive = handeshaking

  protected def handlePeerClosed: Unit = {
    log.warning("Peer closed")
//    context stop self
  }

}

object ToByteString {
  implicit def handshakeToBytes(handshake: Handshake): ByteString =
    ByteString(handshake.marshal.toArray)
  implicit def messageToBytes(message: Message): ByteString =
    ByteString(message.marshal.toArray)
}

object Hex {
  def apply(buf: Array[Byte]): String = buf.map("%02X" format _).mkString(" ")
  def apply(buf: ByteString): String = apply(buf.toArray)
}
