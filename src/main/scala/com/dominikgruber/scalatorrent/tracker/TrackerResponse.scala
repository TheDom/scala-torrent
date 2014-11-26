package com.dominikgruber.scalatorrent.tracker

import com.dominikgruber.scalatorrent.bencode.BencodeParser

/**
 * Descriptions taken from the specification:
 * https://wiki.theory.org/BitTorrentSpecification#Metainfo_File_Structure
 */
sealed abstract class TrackerResponse

case class TrackerResponseWithSuccess
(
  /**
   * Interval in seconds that the client should wait between sending regular
   * requests to the tracker.
   */
  interval: Int,

  /**
   * Minimum announce interval. If present clients must not reannounce more
   * frequently than this.
   */
  minInterval: Option[Int],

  /**
   * A string that the client should send back on its next announcements. If
   * absent and a previous announce sent a tracker id, do not discard the old
   * value; keep using it.
   */
  trackerId: Option[String],

  /**
   * Number of peers with the entire file, i.e. seeders
   */
  complete: Int,

  /**
   * Number of non-seeder peers, aka "leechers"
   */
  incomplete: Int,

  /**
   * List of peers.
   */
  peers: List[Peer],

  /**
   * Similar to failure reason, but the response still gets processed normally.
   * The warning message is shown just like an error.
   */
  warning: Option[String]
) extends TrackerResponse

case class TrackerResponseWithFailure
(
  /**
   * The value is a human-readable error message as to why the request failed.
   */
  reason: String
) extends TrackerResponse

object TrackerResponse {

  def apply(response: String): TrackerResponse =
    apply(BencodeParser(response).get.asInstanceOf[Map[String,Any]])

  def apply(response: Map[String,Any]): TrackerResponse = {
    if (response.contains("failure reason"))
      TrackerResponseWithFailure(response("failure reason").asInstanceOf[String])
    else
      TrackerResponseWithSuccess(
        interval = response("interval").asInstanceOf[Int],
        minInterval =
          if (response.contains("min interval")) Some(response("min interval").asInstanceOf[Int])
          else None,
        trackerId =
          if (response.contains("tracker id")) Some(response("tracker id").asInstanceOf[String])
          else None,
        complete =
          if (response.contains("complete")) response("complete").asInstanceOf[Int]
          else 0,
        incomplete =
          if (response.contains("incomplete")) response("incomplete").asInstanceOf[Int]
          else 0,
        peers = Peer.createList(response("peers")),
        warning =
          if (response.contains("warning message")) Some(response("warning message").asInstanceOf[String])
          else None
      )
  }
}
