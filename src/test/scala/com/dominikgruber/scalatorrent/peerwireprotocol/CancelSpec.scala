package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.util.UnitSpec

class CancelSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = Cancel(1024, 12, 34567)
    msg.marshal should be (Vector[Byte](0, 0, 0, 13, 8, 0, 0, 4, 0, 0, 0, 0, 12, 0, 0, -121, 7))
  }

  "unmarshal" should "work" in {
    val msg = Cancel(1024, 12, 34567)
    Cancel.unmarshal(msg.marshal) should be (Some(msg))
  }

  it should "fail" in {
    Cancel.unmarshal(Vector[Byte](0, 0, 0, 12, 8, 0, 0, 4, 0, 0, 0, 0, 12, 0, 0, -121, 7)) should be (None)
    Cancel.unmarshal(Vector[Byte](0, 0, 0, 13, 7, 0, 0, 4, 0, 0, 0, 0, 12, 0, 0, -121, 7)) should be (None)
  }
}