package com.dominikgruber.scalatorrent

import org.scalatest._
import scala.io.{Codec, Source}

abstract class UnitSpec extends FlatSpec with Matchers {

  def loadTorrentFile(file: String): String = {
    val source = Source.fromURL(getClass.getResource(file))(Codec.ISO8859)
    val sourceString = source.mkString
    source.close()
    sourceString
  }
}