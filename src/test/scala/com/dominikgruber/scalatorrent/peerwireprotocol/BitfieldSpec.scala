package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class BitfieldSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = Bitfield(Vector(true, true, false, false, true, false, true, false, true))
    msg.marshal should be (Vector[Byte](0, 0, 0, 3, 5, -54, -128))
  }

  "unmarshal" should "work" in {
    val msg = Bitfield(Vector(true, true, false, false, true, false, true, false, true))
    val msgEx = Bitfield(Vector(true, true, false, false, true, false, true, false, true, false, false, false, false, false, false, false))
    Bitfield.unmarshal(msg.marshal) should be (Some(msgEx))
  }

  it should "fail" in {
    Bitfield.unmarshal(Vector[Byte](0, 0, 0, 1, 5, -54, -128)) should be (None)
    Bitfield.unmarshal(Vector[Byte](0, 0, 0, 3, 6, -54, -128)) should be (None)
    Bitfield.unmarshal(Vector[Byte](0, 0, 0, 3, 5)) should be (None)
    Bitfield.unmarshal(Vector[Byte](0, 0, 0, 4, 5, -54, -128)) should be (None)
  }
}