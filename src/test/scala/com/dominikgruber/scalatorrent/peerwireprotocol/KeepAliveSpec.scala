package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.util.UnitSpec

class KeepAliveSpec extends UnitSpec {

  "marshal" should "work" in {
    val msg = KeepAlive()
    msg.marshal should be (Vector[Byte](0, 0, 0, 0))
  }

  "unmarshal" should "work" in {
    KeepAlive.unmarshal(KeepAlive().marshal) should be (Some(KeepAlive()))
    KeepAlive.unmarshal(Vector.fill[Byte](4)(0)) should be (Some(KeepAlive()))
  }

  it should "fail" in {
    KeepAlive.unmarshal(Vector.fill[Byte](4)(1)) should be (None)
  }
}