package com.dominikgruber.scalatorrent.util

import com.dominikgruber.scalatorrent.metainfo.{MetaInfo, SingleFileMetaInfo}

object Stub {

  def metaInfo(totalLength: Int, pieceLength: Int): MetaInfo = {
    val fileInfo = SingleFileMetaInfo(Vector.empty, pieceLength, "", None, "", totalLength, None)
    MetaInfo(fileInfo, "", None, None, None, None, None)
  }

}
