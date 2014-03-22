package com.dominikgruber.scalatorrent.bencode

import com.dominikgruber.scalatorrent.UnitSpec

class BencodeParserSpec extends UnitSpec {

  import BencodeParser._

  def assertParse[T](in: String, out: Any, parser: Parser[T]) = {
    BencodeParser.parseAll(parser, in).get should be (out)
    BencodeParser(in).get should be (out)
  }

  def assertParserFailure[T](in: String, parser: Parser[T]) = {
    intercept[RuntimeException] {
      BencodeParser.parseAll(parser, in).get
    }
    intercept[RuntimeException] {
      BencodeParser(in).get
    }
  }

  "BencodeParser" should "detect a string" in {
    assertParse("4:spam", "spam", string)
  }

  it should "not allow an empty string" in {
    assertParserFailure("0:", string)
  }

  it should "consider the given length of the string (longer)" in {
    assertParserFailure("5:spam", string)
  }

  it should "consider the given length of the string (shorter)" in {
    assertParserFailure("3:spam", string)
  }

  it should "detect an integer" in {
    assertParse("i3e", 3, integer)
  }

  it should "detect 0" in {
    assertParse("i0e", 0, integer)
  }

  it should "detect a negative number" in {
    assertParse("i-3e", -3, integer)
  }

  it should "not allow zero padding for postive numbers" in {
    assertParserFailure("i04e", integer)
  }

  it should "not allow zero padding for zero" in {
    assertParserFailure("i00e", integer)
  }

  it should "not allow zero padding for negative numbers" in {
    assertParserFailure("i-01e", integer)
  }

  it should "not allow negative zero" in {
    assertParserFailure("i-0e", integer)
  }

  it should "detect a list of strings" in {
    val in = "l4:spam4:eggse"
    val out = List("spam", "eggs")
    assertParse(in, out, list)
  }

  it should "detect a list of integers" in {
    val in = "li-1ei0ei4ee"
    val out = List(-1, 0, 4)
    assertParse(in, out, list)
  }

  it should "detect a list within a list" in {
    val in = "ll5:scala4:javael4:akka4:playee"
    val out = List(List("scala", "java"), List("akka", "play"))
    assertParse(in, out, list)
  }

  it should "detect a dictionary within a list" in {
    val in = "ld4:spaml1:a1:beee"
    val out = List(Map("spam" -> List("a", "b")))
    assertParse(in, out, list)
  }

  it should "detect a mixed list" in {
    val in = "l4:spami-1el4:spam4:eggsed3:cow3:mooee"
    val out = List("spam", -1, List("spam", "eggs"), Map("cow" -> "moo"))
    assertParse(in, out, list)
  }

  it should "not allow an empty list" in {
    assertParserFailure("le", list)
  }

  it should "detect a dictionary (farm example)" in {
    val in = "d3:cow3:moo4:spam4:eggse"
    val out = Map("cow" -> "moo", "spam" -> "eggs")
    assertParse(in, out, dictionary)
  }

  it should "detect a dictionary with a list value" in {
    val in = "d4:spaml1:a1:bee"
    val out = Map("spam" -> List("a", "b"))
    assertParse(in, out, dictionary)
  }

  it should "detect a dictionary (publisher example)" in {
    val in = "d9:publisher3:bob17:publisher-webpage15:www.example.com18:publisher.location4:homee"
    val out = Map(
      "publisher" -> "bob",
      "publisher-webpage" -> "www.example.com",
      "publisher.location" -> "home"
    )
    assertParse(in, out, dictionary)
  }

  it should "not allow an empty dictionary" in {
    assertParserFailure("de", dictionary)
  }

  it should "fail if only a dictionary key is given" in {
    assertParserFailure("d4:spame", dictionary)
  }
}