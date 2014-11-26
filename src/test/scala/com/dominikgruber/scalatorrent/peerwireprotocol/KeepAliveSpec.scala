package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class KeepAliveSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = KeepAlive()
    msg.marshal should be (Vector[Byte](0, 0, 0, 0))
  }
}