package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class HaveSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = Have(423423023)
    msg.marshal should be (Vector[Byte](0, 0, 0, 5, 4, 25, 60, -20, 47))
  }
}