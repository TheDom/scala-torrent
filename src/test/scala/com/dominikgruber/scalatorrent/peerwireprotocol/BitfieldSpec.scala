package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class BitfieldSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = Bitfield(Vector(true, true, false, false, true, false, true, false, true))
    msg.marshal should be (Vector[Byte](0, 0, 0, 3, 5, -54, -128))
  }
}