package com.dominikgruber.scalatorrent.metainfo

import com.dominikgruber.scalatorrent.UnitSpec
import java.util.Date
import scala.io.Source

class MetainfoSpec extends UnitSpec {

  "loadFromBencodedString" should "parse the Ubuntu demo torrent correctly" in {
    val source = Source.fromURL(getClass.getResource("/metainfo/ubuntu-12.04.4-server-amd64.iso.torrent"))
    val sourceString = source.mkString
    source.close()
    val in = Metainfo.loadFromBencodedString(sourceString)
    val out = Metainfo(
      MetainfoInfoSingleFile("363f99a148c0e9b2d299ea8e548cfdf18286e858", 524288, "demo", None, "ubuntu-12.04.4-server-amd64.iso", 711983104, None),
      "http://torrent.ubuntu.com:6969/announce",
      Some(List(List("http://torrent.ubuntu.com:6969/announce"), List("http://ipv6.torrent.ubuntu.com:6969/announce"))),
      Some(new Date(1391706765000l)),
      Some("Ubuntu CD releases.ubuntu.com"),
      None,
      None
    )
    in should be (out)
    out.bencodedString.get should be (sourceString)
  }

  it should "parse the Killers_from_space_archive demo torrent correctly" in {
    val source = Source.fromURL(getClass.getResource("/metainfo/Killers_from_space_archive.torrent"))
    val sourceString = source.mkString
    source.close()
    val in = Metainfo.loadFromBencodedString(sourceString)
    source.close()
    val out = Metainfo(
      MetainfoInfoMultiFile(
        "4c447c6039bb9c59e5c0ca1acd50ba642249e592",
        2097152,
        "demo",
        None,
        "Killers_from_space",
        List(
          FileInfo(408587, None, List("Killers_from_space.gif")),
          FileInfo(1688565, None, List(".____padding_file", "0")),
          FileInfo(1750300672, None, List("Killers_from_space.mpeg")),
          FileInfo(821248, None, List(".____padding_file", "1")),
          FileInfo(302886601, None, List("Killers_from_space.ogv")),
          FileInfo(1200439, None, List(".____padding_file", "2")),
          FileInfo(309212374, None, List("Killers_from_space_512kb.mp4")),
          FileInfo(1166122, None, List(".____padding_file", "3")),
          FileInfo(1233, None, List("Killers_from_space_meta.xml")),
          FileInfo(2095919, None, List(".____padding_file", "4")))),
      "http://bt1.archive.org:6969/announce",
      Some(List(List("http://bt1.archive.org:6969/announce"), List("http://bt2.archive.org:6969/announce"))),
      Some(new Date(1343714774000l)),
      Some("This content hosted at the Internet Archive at http://archive.org/details/Killers_from_space Files may have changed, which prevents torrents from downloading correctly or completely; please check for an updated torrent at http://archive.org/download/Killers_from_space/Killers_from_space_archive.torrent Note: retrieval usually requires a client that supports webseeding (GetRight style). Note: many Internet Archive torrents contain a 'pad file' directory. This directory and the files within it may be erased once retrieval completes. Note: the file Killers_from_space_meta.xml contains metadata about this torrent's contents."),
      Some("ia_make_torrent"),
      None
    )
    in should be (out)

    // This test fails currently because the .torrent file contains non-standard
    // entries in the info dictionary ("crc32", "md5", and "mtime")
    // out.bencodedString.get should be (sourceString)
  }
}
