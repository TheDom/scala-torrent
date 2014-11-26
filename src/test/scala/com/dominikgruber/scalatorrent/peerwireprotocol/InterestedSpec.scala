package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class InterestedSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = Interested()
    msg.marshal should be (Vector[Byte](0, 0, 0, 1, 2))
  }
}