package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class UnchokeSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = Unchoke()
    msg.marshal should be (Vector[Byte](0, 0, 0, 1, 1))
  }
}