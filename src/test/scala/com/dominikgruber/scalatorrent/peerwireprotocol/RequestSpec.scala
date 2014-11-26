package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class RequestSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = Request(256, 512, 1024)
    msg.marshal should be (Vector[Byte](0, 0, 0, 13, 6, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 4, 0))
  }

  "unmarshal" should "work" in {
    val msg = Request(256, 512, 1024)
    Request.unmarshal(msg.marshal) should be (Some(msg))
  }

  it should "fail" in {
    Unchoke.unmarshal(Vector[Byte](0, 0, 0, 14, 6, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 4, 0)) should be (None)
  }
}