package com.dominikgruber.scalatorrent.peerwireprotocol

/**
 * The handshake is a required message and must be the first message transmitted
 * by the client. It is (49+len(pstr)) bytes long.
 *
 * handshake: <pstrlen><pstr><reserved><info_hash><peer_id>
 *
 * - pstrlen: string length of <pstr>, as a single raw byte
 * - pstr: string identifier of the protocol
 * - reserved: eight (8) reserved bytes. All current implementations use all
 * zeroes. Each bit in these bytes can be used to change the behavior of the
 * protocol. An email from Bram suggests that trailing bits should be used first,
 * so that leading bits may be used to change the meaning of trailing bits.
 * - info_hash: 20-byte SHA1 hash of the info key in the metainfo file. This is
 * the same info_hash that is transmitted in tracker requests.
 * - peer_id: 20-byte string used as a unique ID for the client. This is usually
 * the same peer_id that is transmitted in tracker requests (but not always e.g.
 * an anonymity option in Azureus).
 *
 * In version 1.0 of the BitTorrent protocol, pstrlen = 19, and pstr =
 * "BitTorrent protocol".
 */
case class Handshake(infoHash: Vector[Byte], peerId: String) {

  def marshal: Vector[Byte] = {
    Vector.concat(
      Vector[Byte](19),
      "BitTorrent protocol".getBytes("ISO-8859-1"),
      Vector.fill[Byte](8)(0),
      infoHash,
      peerId.getBytes("ISO-8859-1")
    )
  }

  /**
   * Hex string representation of the SHA1 value
   */
  lazy val infoHashString: String = infoHash.map("%02X" format _).mkString
}

object Handshake {

  def parse(message: Vector[Byte]): Option[Handshake] = {
    if (message.length == 68) {
      val pstrlen = message(0)
      val pstr = new String(message.slice(1, 1 + pstrlen).toArray, "ISO-8859-1")
      if (pstr == "BitTorrent protocol") {
        val infoHash = message.slice(message.length - 40, message.length - 20)
        val peerId = new String(message.slice(message.length - 20, message.length).toArray, "ISO-8859-1")
        Some(Handshake(infoHash, peerId))
      } else None
    } else None
  }
}