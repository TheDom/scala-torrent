package com.dominikgruber.scalatorrent.bencode

import com.dominikgruber.scalatorrent.UnitSpec

class BencodeEncoderSpec extends UnitSpec {

  import BencodeEncoder._

  "string" should "return a valid string" in {
    string("SCALA-torrent").get should be ("13:SCALA-torrent")
  }

  it should "not work on an empty string" in {
    string("") should be (None)
  }

  "integer" should "return a valid string" in {
    integer(42) should be ("i42e")
  }

  it should "work on a negative number" in {
    integer(-42) should be ("i-42e")
  }

  it should "work on zero" in {
    integer(0) should be ("i0e")
  }

  "list" should "work on a list of strings" in {
    val l = List("scala", "torrent")
    list(l).get should be ("l5:scala7:torrente")
  }

  it should "work on a list of integers" in {
    val l = List(-42, 0, 42)
    list(l).get should be ("li-42ei0ei42ee")
  }

  it should "work on a list of lists" in {
    val l = List(List(0, 42), List("0", "42"))
    list(l).get should be ("lli0ei42eel1:02:42ee")
  }

  it should "work on a mixed list" in {
    val l = List(-42, "0", "scala", List(0, "0"))
    list(l).get should be ("li-42e1:05:scalali0e1:0ee")
  }

  it should "only work on allowed values" in {
    val l = List(42, "")
    list(l) should be (None)
  }

  "dictionary" should "work on a map with mixed values" in {
    val m = Map("a" -> "b", "c" -> 42, "d" -> List(32, 42), "e" -> Map("i" -> "j"))
    dictionary(m).get should be ("d1:a1:b1:ci42e1:dli32ei42ee1:ed1:i1:jee")
  }

  it should "not allow an empty string a key" in {
    val m = Map("" -> "goto fail")
    dictionary(m) should be (None)
  }

  it should "not allow unknown types as values" in {
    val m = Map("a" -> 42.0)
    dictionary(m) should be (None)
  }

  it should "sort the keys as raw strings, not alphanumerics" in {
    val m = Map("b" -> 1, "a" -> 2, "ä" -> 3, "O" -> 4, "A" -> 5)
    dictionary(m).get should be ("d1:Ai5e1:Oi4e1:ai2e1:bi1e1:äi3ee")
  }
}
