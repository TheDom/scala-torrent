package com.dominikgruber.scalatorrent.metainfo

import com.dominikgruber.scalatorrent.bencode.BencodeEncoder
import scala.collection.mutable

/**
 * Descriptions taken from the specification:
 * https://wiki.theory.org/BitTorrentSpecification#Metainfo_File_Structure
 */
sealed trait MetainfoInfo
{
  /**
   * Number of bytes in each piece.
   */
  def pieceLength: Int

  /**
   * String consisting of the concatenation of all 20-byte SHA1 hash values,
   * one per piece (byte string, i.e. not urlencoded).
   */
  def pieces: String

  /**
   * If it is set to "true", the client MUST publish its
   * presence to get other peers ONLY via the trackers explicitly described in
   * the metainfo file. If this field is set to "false" or is not present, the
   * client may obtain peer from other means, e.g. PEX peer exchange, dht.
   * Here, "private" may be read as "no external peer source".
   */
  def privateTorrent: Option[Boolean]

  /**
   * Convenience Helper
   * @return Is this a private torrent?
   */
  def isPrivateTorrent: Boolean = privateTorrent.getOrElse(false)

  /**
   * Convert the content to a map conforming to the .torrent file standard
   */
  def toMap: Map[String,Any]

  /**
   * Bencoded string conforming to the .torrent file standard
   */
  def bencodedString: Option[String] =
    BencodeEncoder(toMap)

  /**
   * SHA1 value of the bencoded string
   */
  def SHA1: Option[String] = {
    bencodedString match {
      case Some(s: String) => {
        val md = java.security.MessageDigest.getInstance("SHA-1")
        Some(md.digest(s.getBytes).map("%02x".format(_)).mkString)
      }
      case None => None
    }
  }
}

case class MetainfoInfoSingleFile
(
  pieceLength: Int,
  pieces: String,
  privateTorrent: Option[Boolean],

  /**
   * The filename. This is purely advisory.
   */
  name: String,

  /**
   * Length of the file in bytes.
   */
  length: Int,

  /**
   * A 32-character hexadecimal string corresponding to the MD5 sum of the file.
   * This is not used by BitTorrent at all, but it is included by some programs
   * for greater compatibility.
   */
  md5sum: Option[String]

) extends MetainfoInfo {

  def toMap: Map[String,Any] = {
    val map: mutable.Map[String,Any] = mutable.Map(
      "piece length" -> pieceLength,
      "pieces" -> pieces,
      "name" -> name,
      "length" -> length
    )
    if (privateTorrent.isDefined) map += ("private" -> privateTorrent.get)
    if (md5sum.isDefined) map += ("md5sum" -> md5sum.get)
    map.toMap
  }
}

case class MetainfoInfoMultiFile
(
  pieceLength: Int,
  pieces: String,
  privateTorrent: Option[Boolean],

  /**
   * The file path of the directory in which to store all the files. This is
   * purely advisory.
   */
  name: String,

  files: List[FileInfo]

) extends MetainfoInfo {

  def toMap: Map[String,Any] = {
    val map: mutable.Map[String,Any] = mutable.Map(
      "piece length" -> pieceLength,
      "pieces" -> pieces,
      "name" -> name,
      "files" -> files.map(_.toMap)
    )
    if (privateTorrent.isDefined) map += ("private" -> privateTorrent.get)
    map.toMap
  }
}

object MetainfoInfo {

  def create(info: Map[String,Any]): MetainfoInfo = {
    if (info.contains("length"))
      MetainfoInfoSingleFile(
        pieceLength = info("piece length").asInstanceOf[Int],
        pieces = info("pieces").asInstanceOf[String],
        privateTorrent =
          if (info.contains("private")) Some(info("private").asInstanceOf[Int] == 1)
          else None,
        name = info("name").asInstanceOf[String],
        length = info("length").asInstanceOf[Int],
        md5sum =
          if (info.contains("md5sum")) Some(info("md5sum").asInstanceOf[String])
          else None
      )
    else if (info.contains("files"))
      MetainfoInfoMultiFile(
        pieceLength = info("piece length").asInstanceOf[Int],
        pieces = info("pieces").asInstanceOf[String],
        privateTorrent =
          if (info.contains("private")) Some(info("private").asInstanceOf[Int] == 1)
          else None,
        name = info("name").asInstanceOf[String],
        files = FileInfo.create(info("files").asInstanceOf[List[Map[String,Any]]])
      )
    else
      throw new IllegalArgumentException("Provided file is not a valid .torrent file.")
  }
}