package com.dominikgruber.scalatorrent.metainfo

import com.dominikgruber.scalatorrent.bencode.BencodeParser
import java.util.Date
import scala.io.{Codec, Source}

/**
 * Descriptions taken from the specification:
 * https://wiki.theory.org/BitTorrentSpecification#Metainfo_File_Structure
 */
case class Metainfo
(
  /**
   * A dictionary that describes the file(s) of the torrent. There are two
   * possible forms: one for the case of a 'single-file' torrent with no
   * directory structure, and one for the case of a 'multi-file' torrent.
   */
  info: MetainfoInfo,

  /**
   * The announce URL of the tracker
   */
  announce: String,

  /**
   * This is an extention to the official specification, offering
   * backwards-compatibility. (list of lists of strings).
   */
  announceList: Option[List[List[String]]],

  /**
   * The creation time of the torrent.
   */
  creationDate: Option[Date],

  /**
   * Free-form textual comments of the author.
   */
  comment: Option[String],

  /**
   * Name and version of the program used to create the .torrent.
   */
  createdBy: Option[String],

  /**
   * The string encoding format used to generate the pieces part of the info
   * dictionary in the .torrent metafile
   */
  encoding: Option[String]
)

object Metainfo {

  def loadFromFile(file: String): Metainfo = {
    val source = Source.fromFile(file)(Codec.ISO8859)
    val info = source.mkString
    source.close()
    loadFromBencodedString(info)
  }

  def loadFromBencodedString(bencode: String) = {
    val info = BencodeParser(bencode).get.asInstanceOf[Map[String,Any]]
    create(info)
  }

  def create(info: Map[String,Any]): Metainfo = {
    Metainfo(
      info = MetainfoInfo.create(info("info").asInstanceOf[Map[String,Any]]),
      announce = info("announce").asInstanceOf[String],
      announceList =
        if (info.contains("announce-list")) Some(info("announce-list").asInstanceOf[List[List[String]]])
        else None,
      creationDate =
        if (info.contains("creation date")) Some(new Date(info("creation date").asInstanceOf[Int].toLong * 1000l))
        else None,
      comment =
        if (info.contains("comment")) Some(info("comment").asInstanceOf[String])
        else None,
      createdBy =
        if (info.contains("created by")) Some(info("created by").asInstanceOf[String])
        else None,
      encoding =
        if (info.contains("encoding")) Some(info("encoding").asInstanceOf[String])
        else None
    )
  }
}