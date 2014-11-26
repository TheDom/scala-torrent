package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class ChokeSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = Choke()
    msg.marshal should be (Vector[Byte](0, 0, 0, 1, 0))
  }

  "unmarshal" should "work" in {
    Choke.unmarshal(Choke().marshal) should be (Some(Choke()))
  }

  it should "fail" in {
    Choke.unmarshal(Vector[Byte](0, 0, 0, 0, 0)) should be (None)
    Choke.unmarshal(Vector[Byte](0, 0, 0, 1, 1)) should be (None)
  }
}