package com.dominikgruber.scalatorrent.peerwireprotocol

import com.dominikgruber.scalatorrent.UnitSpec

class MessageSpec extends UnitSpec {

  "unmarshal" should "fail on invalid input" in {
    Message.unmarshal(Vector.fill[Byte](2)(0)) should be (None)
    Message.unmarshal(Vector.fill[Byte](4)(1)) should be (None)
  }

  it should "detect Bitfield" in {
    val msg = Bitfield(Vector(true, false, true, true))
    Message.unmarshal(msg.marshal) should be (Some(Bitfield(Vector(true, false, true, true, false, false, false, false))))
  }

  it should "detect Cancel" in {
    testUnmarshal(Cancel(89, 0, 12))
  }

  it should "detect Choke" in {
    testUnmarshal(Choke())
  }

  it should "detect Have" in {
    testUnmarshal(Have(1023))
  }

  it should "detect Interested" in {
    testUnmarshal(Interested())
  }

  it should "detect KeepAlive" in {
    testUnmarshal(KeepAlive())
  }

  it should "detect NotInterested" in {
    testUnmarshal(NotInterested())
  }

  it should "detect Piece" in {
    testUnmarshal(Piece(28, 100, Vector[Byte](10, -20, 12)))
  }

  it should "detect Request" in {
    testUnmarshal(Request(10, 200, 2323))
  }

  it should "detect Unchoke" in {
    testUnmarshal(Unchoke())
  }

  def testUnmarshal(msg: Message) = {
    Message.unmarshal(msg.marshal) should be (Some(msg))
  }
}