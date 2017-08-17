package com.dominikgruber.scalatorrent.tracker

import com.dominikgruber.scalatorrent.util.UnitSpec

class PeerSpec extends UnitSpec {

  "Peer" should "parse a single peer" in {
    val str = Map("peer id" -> "-SC0001-123456789012", "ip" -> "10.0.0.1", "port" -> 65535)
    Peer(str) should be (Peer(Some("-SC0001-123456789012"), "10.0.0.1", 65535))
  }

  it should "parse a single peer with no peer id" in {
    val str = Map("ip" -> "www.dominikgruber.com", "port" -> 1)
    Peer(str) should be (Peer(None, "www.dominikgruber.com", 1))
  }

  it should "parse a list of peers" in {
    val list = List(Map("peer id" -> "-SC0001-123456789012", "ip" -> "10.0.0.1", "port" -> 65535), Map("ip" -> "www.dominikgruber.com", "port" -> 1))
    Peer.createList(list) should be (List(Peer(Some("-SC0001-123456789012"), "10.0.0.1", 65535), Peer(None, "www.dominikgruber.com", 1)))
  }

  it should "parse a string with compact format" in {
    val byteArr = Array("4F", "8D", "AD", "AE", "C2", "90", "4F", "8D", "AD", "B0", "65", "8D").map(Integer.parseInt(_, 16).toByte)
    Peer.createList(new String(byteArr, "ISO-8859-1")).reverse should be (List(Peer(None, "79.141.173.174", 49808), Peer(None, "79.141.173.176", 25997)))
  }
}
