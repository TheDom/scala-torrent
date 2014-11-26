package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class HaveSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = Have(423423023)
    msg.marshal should be (Vector[Byte](0, 0, 0, 5, 4, 25, 60, -20, 47))
  }

  "unmarshal" should "work" in {
    val msg = Have(423423023)
    Have.unmarshal(msg.marshal) should be (Some(msg))
  }

  it should "fail" in {
    Have.unmarshal(Vector[Byte](0, 0, 0, 5, 4, 25, 60, -20)) should be (None)
    Have.unmarshal(Vector[Byte](0, 0, 2, 5, 4, 25, 60, -20, 47)) should be (None)
    Have.unmarshal(Vector[Byte](0, 0, 0, 6, 4, 25, 60, -20, 47)) should be (None)
    Have.unmarshal(Vector[Byte](0, 0, 0, 5, 3, 25, 60, -20, 47)) should be (None)
  }
}