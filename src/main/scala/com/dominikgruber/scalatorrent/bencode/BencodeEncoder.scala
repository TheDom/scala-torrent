package com.dominikgruber.scalatorrent.bencode

import scala.annotation.tailrec

/**
 * Takes an input and transforms it into a bencoded string.
 */
object BencodeEncoder {

  /**
   * @see [[BencodeParser.string]]
   */
  def string(str: String): Option[String] = {
    val l = str.length
    if (l > 0) Some(l + ":" + str)
    else None
  }

  /**
   * @see [[BencodeParser.integer]]
   */
  def integer(i: Int): String =
    "i" + i + "e"

  /**
   * @see [[BencodeParser.list]]
   */
  def list(l: List[Any]): Option[String] = {
    @tailrec
    def inner(l: List[Any], acc: String): Option[String] = l match {
      case head :: tail => encode(head) match {
        case Some(x) => inner(tail, acc + x)
        case None => None
      }
      case _ => Some("l" + acc + "e")
    }
    inner(l, "")
  }

  /**
   * @see [[BencodeParser.dictionary]]
   */
  def dictionary(m: Map[String,Any]): Option[String] = {
    @tailrec
    def inner(l: List[(String,Any)], acc: String): Option[String] = l match {
      case (s, v) :: tail => (apply(s), encode(v)) match {
        case (Some(str1), Some(str2)) => inner(tail, acc + str1 + str2)
        case _ => None
      }
      case _ => Some("d" + acc + "e")
    }
    inner(m.toList.sortBy(_._1), "")
  }

  def encode(input: Any): Option[String] = input match {
    case s: String => string(s)
    case i: Int => Option(integer(i))
    case l: List[Any] => list(l)
    case d: Map[_,_] => d.toList match { // Workaround for type erasure
      case ((s: String, v: Any)) :: tail => dictionary(d.asInstanceOf[Map[String,Any]])
      case _ => None
    }
    case _ => None
  }

  def apply(input: String): Option[String] =
    string(input)

  def apply(input: Int): String =
    integer(input)

  def apply(input: List[Any]): Option[String] =
    list(input)

  def apply(input: Map[String,Any]): Option[String] =
    dictionary(input)
}
