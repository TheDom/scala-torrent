package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.util.UnitSpec

class UnchokeSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = Unchoke()
    msg.marshal should be (Vector[Byte](0, 0, 0, 1, 1))
  }

  "unmarshal" should "work" in {
    Unchoke.unmarshal(Unchoke().marshal) should be (Some(Unchoke()))
  }

  it should "fail" in {
    Unchoke.unmarshal(Vector[Byte](0, 0, 1, 0, 1)) should be (None)
  }
}