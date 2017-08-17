package com.dominikgruber.scalatorrent.util

import com.dominikgruber.scalatorrent.metainfo.{MetaInfo, SingleFileMetaInfo}

object Mocks {

  def metaInfo(totalLength: Int = 0, pieceLength: Int = 1): MetaInfo = {
    val fileInfo = SingleFileMetaInfo(infoHash, pieceLength, "", None, "", totalLength, None)
    MetaInfo(fileInfo, "", None, None, None, None, None)
  }

  val infoHash = Vector.fill(20)(0.toByte)

}
