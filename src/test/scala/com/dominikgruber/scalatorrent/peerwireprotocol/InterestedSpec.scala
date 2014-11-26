package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class InterestedSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = Interested()
    msg.marshal should be (Vector[Byte](0, 0, 0, 1, 2))
  }

  "unmarshal" should "work" in {
    Interested.unmarshal(Interested().marshal) should be (Some(Interested()))
    Interested.unmarshal(Vector[Byte](0, 0, 0, 1, 2)) should be (Some(Interested()))
  }

  it should "fail" in {
    Interested.unmarshal(Vector[Byte](0, 0, 0, 0, 2)) should be (None)
    Interested.unmarshal(Vector[Byte](0, 0, 0, 2, 2)) should be (None)
  }
}