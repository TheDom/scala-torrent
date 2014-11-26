package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class CancelSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = Cancel(1024, 12, 34567)
    msg.marshal should be (Vector[Byte](0, 0, 0, 13, 8, 0, 0, 4, 0, 0, 0, 0, 12, 0, 0, -121, 7))
  }
}