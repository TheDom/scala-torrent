package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class NotInterestedSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = NotInterested()
    msg.marshal should be (Vector[Byte](0, 0, 0, 1, 3))
  }
}