package com.dominikgruber.scalatorrent.bencode

import scala.util.parsing.combinator._

/**
 * Descriptions taken from the specification:
 * https://wiki.theory.org/BitTorrentSpecification#Bencoding
 */
object BencodeParser extends RegexParsers {

  override def skipWhitespace = false

  /**
   * Bencoded strings are encoded as follows:
   * <string length encoded in base ten ASCII>:<string data>, or key:value
   * Note that there is no constant beginning delimiter, and no ending
   * delimiter.
   */
  def string: Parser[String] = ("""[1-9]\d*""".r <~ ":") into { count =>
    repN(count.toInt, "(?s).".r) ^^ (_.mkString)
  }

  /**
   * Integers are encoded as follows: i<integer encoded in base ten ASCII>e
   * The initial i and trailing e are beginning and ending delimiters. You can
   * have negative numbers such as i-3e. Only the significant digits should be
   * used, one cannot pad the Integer with zeroes. such as i04e. However, i0e is
   * valid.
   */
  def integer: Parser[Int] = "i" ~> """(0|\-?[1-9]\d*)""".r <~ "e" ^^ (_.toInt)

  /**
   * Lists are encoded as follows: l<bencoded values>e
   * The initial l and trailing e are beginning and ending delimiters. Lists may
   * contain any bencoded type, including integers, strings, dictionaries, and
   * even lists within other lists.
   */
  def list: Parser[List[Any]] = "l" ~> rep1(bencodeElem) <~ "e"

  /**
   * Dictionaries are encoded as follows: d<bencoded string><bencoded element>e
   * The initial d and trailing e are the beginning and ending delimiters. Note
   * that the keys must be bencoded strings. The values may be any bencoded
   * type, including integers, strings, lists, and other dictionaries. Keys must
   * be strings and appear in sorted order (sorted as raw strings, not
   * alphanumerics). The strings should be compared using a binary comparison,
   * not a culture-specific "natural" comparison.
   *
   * @todo Ensure keys appear in sorted order
   */
  def dictionary: Parser[Map[String,Any]] =
    "d" ~> rep1(string ~ bencodeElem) <~ "e" ^^ (_.map(x => (x._1, x._2)).toMap)

  def bencodeElem = string | integer | list | dictionary

  def apply(input: String) = parseAll(bencodeElem, input)
}
