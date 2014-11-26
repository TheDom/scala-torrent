package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class NotInterestedSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = NotInterested()
    msg.marshal should be (Vector[Byte](0, 0, 0, 1, 3))
  }

  "unmarshal" should "work" in {
    NotInterested.unmarshal(NotInterested().marshal) should be (Some(NotInterested()))
    NotInterested.unmarshal(Vector[Byte](0, 0, 0, 1, 3)) should be (Some(NotInterested()))
  }

  it should "fail" in {
    NotInterested.unmarshal(Vector[Byte](0, 0, 0, 0, 3)) should be (None)
    NotInterested.unmarshal(Vector[Byte](0, 0, 0, 1, 4)) should be (None)
  }
}