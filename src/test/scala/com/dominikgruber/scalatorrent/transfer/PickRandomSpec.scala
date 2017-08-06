package com.dominikgruber.scalatorrent.transfer

import com.dominikgruber.scalatorrent.UnitSpec
import com.dominikgruber.scalatorrent.transfer.PickRandom._

class PickRandomSpec extends UnitSpec {


  it should "return None for an empty Seq" in {
    Seq()
      .randomIndexOf(false) shouldBe None
  }

  it should "work for a seq of 1" in {
    Seq(true)
      .randomIndexOf(false) shouldBe None
    Seq(false)
      .randomIndexOf(false) should contain(0)
  }

  it should "work for a seq of 2" in {
    Seq(true, true)
      .randomIndexOf(false) shouldBe None
    Seq(true, false)
      .randomIndexOf(false) should contain(1)
    Seq(false, true)
      .randomIndexOf(false) should contain(0)
    Seq(false, false)
      .randomIndexOf(false) should contain oneOf(0, 1)
  }


  it should "work for a seq of 3" in {
    Seq(true, true, true)
      .randomIndexOf(false) shouldBe None
    Seq(false, true, true)
      .randomIndexOf(false) should contain(0)
    Seq(true, false, false)
      .randomIndexOf(false) should contain oneOf(1, 2)
  }

  it should "work for a seq of 4" in {
    Seq(true, true, true, true)
      .randomIndexOf(false) shouldBe None
    Seq(true, true, false, true)
      .randomIndexOf(false) should contain(2)
    Seq(false, false, true, false)
      .randomIndexOf(false) should contain oneOf(0, 1, 3)
  }

}
