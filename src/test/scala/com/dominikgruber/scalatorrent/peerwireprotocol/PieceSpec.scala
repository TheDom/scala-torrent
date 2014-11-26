package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class PieceSpec extends UnitSpec  {

  "marshal" should "work" in {
    val msg = Piece(256, 512, Vector[Byte](90, 78, 10, 20))
    msg.marshal should be (Vector[Byte](0, 0, 0, 13, 7, 0, 0, 1, 0, 0, 0, 2, 0, 90, 78, 10, 20))
  }
}