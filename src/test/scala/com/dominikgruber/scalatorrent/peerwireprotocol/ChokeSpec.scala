package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class ChokeSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = Choke()
    msg.marshal should be (Vector[Byte](0, 0, 0, 1, 0))
  }
}