package com.dominikgruber.scalatorrent.metainfo

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
  def privateTorrent: Boolean
}

case class MetainfoInfoSingleFile
(
  pieceLength: Int,
  pieces: String,
  privateTorrent: Boolean,

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
) extends MetainfoInfo

case class MetainfoInfoMultiFile
(
  pieceLength: Int,
  pieces: String,
  privateTorrent: Boolean,

  /**
   * The file path of the directory in which to store all the files. This is
   * purely advisory.
   */
  name: String,

  files: List[FileInfo]
) extends MetainfoInfo

object MetainfoInfo {

  def create(info: Map[String,Any]): MetainfoInfo = {
    if (info.contains("length"))
      MetainfoInfoSingleFile(
        pieceLength = info("piece length").asInstanceOf[Int],
        pieces = info("pieces").asInstanceOf[String],
        privateTorrent =
          if (info.contains("private")) info("private").asInstanceOf[Int] == 1
          else false,
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
          if (info.contains("private")) info("private").asInstanceOf[Int] == 1
          else false,
        name = info("name").asInstanceOf[String],
        files = FileInfo.create(info("files").asInstanceOf[List[Map[String,Any]]])
      )
    else
      throw new IllegalArgumentException("Provided file is not a valid .torrent file.")
  }
}