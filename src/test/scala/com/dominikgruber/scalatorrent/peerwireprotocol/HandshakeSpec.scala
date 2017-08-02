package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec
import com.dominikgruber.scalatorrent.metainfo.MetaInfo

class HandshakeSpec extends UnitSpec {

  lazy val peerId = "-SC0001-012345678901"
  lazy val exampleMarshaledHandshake = Vector[Byte](19, 66, 105, 116, 84, 111, 114, 114, 101, 110, 116, 32, 112, 114, 111, 116, 111, 99, 111, 108, 0, 0, 0, 0, 0, 0, 0, 0, 54, 63, -103, -95, 72, -64, -23, -78, -46, -103, -22, -114, 84, -116, -3, -15, -126, -122, -24, 88, 45, 83, 67, 48, 48, 48, 49, 45, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 48, 49)

  "marshal" should "work with the example torrent" in {
    val handshake = Handshake(exampleMetainfo.fileInfo.infoHash, peerId)
    handshake.marshal should be (exampleMarshaledHandshake)
  }

  "unmarshal" should "work with the example torrent" in {
    Handshake.parse(exampleMarshaledHandshake) should be (Some(Handshake(exampleMetainfo.fileInfo.infoHash, peerId)))
  }

  def exampleMetainfo: MetaInfo = {
    val sourceString = loadTorrentFile("/metainfo/ubuntu-12.04.4-server-amd64.iso.torrent")
    MetaInfo(sourceString)
  }
}