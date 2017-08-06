package com.dominikgruber.scalatorrent.tracker

import java.net.{InetAddress, InetSocketAddress}

/**
 * Descriptions taken from the specification:
 * https://wiki.theory.org/BitTorrentSpecification#Metainfo_File_Structure
 */
case class Peer
(
 /**
  * Peer's self-selected ID
  */
  peerId: Option[String],

  /**
   * Peer's IP address either IPv6 (hexed) or IPv4 (dotted quad) or DNS name (string)
   */
  ip: String,

  /**
   * Peer's port number
   */
  port: Int

) {
  lazy val inetSocketAddress = new InetSocketAddress(ip, port)
}

object Peer {

  def apply(peer: Map[String,Any]): Peer =
    Peer(
      peerId =
        if (peer.contains("peer id")) Some(peer("peer id").asInstanceOf[String])
        else None,
      ip = peer("ip").asInstanceOf[String],
      port = peer("port").asInstanceOf[Int]
    )

  def createList(peers: Any): List[Peer] = peers match {
    case s: String => createList(s)
    case l: List[_] => createList(l.asInstanceOf[List[Map[String, Any]]])
    case _ => Nil
  }

  /**
   * Instead of using the dictionary model, the peers value may be a string
   * consisting of multiples of 6 bytes. First 4 bytes are the IP address and
   * last 2 bytes are the port number. All in network (big endian) notation.
   */
  def createList(peers: String): List[Peer] = {
    val (peerList, _) = peers.getBytes("ISO-8859-1").foldLeft((List[Peer](), Array[Byte]()))((z, byte) => {
      if (z._2.length < 5) (z._1, z._2 :+ byte)
      else {
        val ip = InetAddress.getByAddress(z._2.take(4))
        val port = BigInt(1, z._2.drop(4) :+ byte)
        val peer = Peer(None, ip.toString.drop(1), port.toInt)
        (peer :: z._1, Array[Byte]()) // This reverses the peer order but avoids appending to the List
      }
    })
    peerList
  }

  /**
   * The value is a list of dictionaries.
   */
  def createList(peers: List[Map[String,Any]]): List[Peer] =
    peers.map(p => apply(p))
}