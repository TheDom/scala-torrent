package com.dominikgruber.scalatorrent.actor

import akka.actor.{ActorRef, Actor}
import java.nio.charset.Charset
import java.net.URLEncoder
import spray.client.pipelining._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import spray.http.{HttpCharsets, HttpRequest, HttpResponse, Uri}
import spray.http.Uri.Query
import scala.util.{Failure, Success}
import com.dominikgruber.scalatorrent.metainfo.MetaInfo
import com.dominikgruber.scalatorrent.tracker.TrackerResponse

object Tracker {
  case class SendEventStarted(downloaded: Long, uploaded: Long)
  case class SendEventStopped(downloaded: Long, uploaded: Long)
  case class SendEventCompleted(downloaded: Long, uploaded: Long)

  case class TrackerResponseReceived(res: TrackerResponse)
  case class TrackerConnectionFailed(msg: String)

  object TrackerEvent extends Enumeration {
    type TrackerEvent = Value
    val Started, Stopped, Completed = Value
  }
}

class Tracker(metainfo: MetaInfo, peerId: String, portIn: Int) extends Actor {
  import Tracker._
  import TrackerEvent._

  override def receive = {
    case SendEventStarted(dl, ul) => // from Torrent
      sendRequest(Started, dl, ul, sender())
    case SendEventStopped(dl, ul) => // from ???
      sendRequest(Stopped, dl, ul, sender())
    case SendEventCompleted(dl, ul) => // from ???
      sendRequest(Completed, dl, ul, sender())
  }

  private def sendRequest(event: TrackerEvent, downloaded: Long, uploaded: Long, requestor: ActorRef) = {
    // Build query manually so the encoding is properly handled
    val paramsStr = getRequestParams(event, downloaded, uploaded).foldLeft("")((z, m) => z + "&" + m._1 + "=" + m._2).drop(1)
    val query = Query(paramsStr, Charset.forName("ISO-8859-1"), Uri.ParsingMode.RelaxedWithRawQuery)
    val uri = Uri(metainfo.announce).withQuery(query)

    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive

    pipeline(Get(uri)) onComplete {
      case Success(res: HttpResponse) =>
        val resString = res.entity.asString(HttpCharsets.`ISO-8859-1`)
        requestor ! TrackerResponseReceived(TrackerResponse(resString))
      case Failure(error) =>
        requestor ! TrackerConnectionFailed(error.getMessage)
    }
  }

  private def getRequestParams(event: TrackerEvent, downloaded: Long, uploaded: Long): Map[String,String] = {
    val eventStr = event match {
      case Started => "started"
      case Stopped => "stopped"
      case Completed => "completed"
    }
    Map(
      /**
       * 20-byte SHA1 hash of the value of the info key from the Metainfo file.
       */
      "info_hash" -> URLEncoder.encode(new String(metainfo.fileInfo.infoHash.toArray, "ISO-8859-1"), "ISO-8859-1"),

      "peer_id" -> peerId,

      /**
       * The port number that the client is listening on. Ports reserved for
       * BitTorrent are typically 6881-6889. Clients may choose to give up if it
       * cannot establish a port within this range.
       */
      "port" -> portIn.toString,

      /**
       * The total amount uploaded (since the client sent the 'started' event to
       * the tracker) in base ten ASCII. While not explicitly stated in the
       * official specification, the concensus is that this should be the total
       * number of bytes uploaded.
       */
      "uploaded" -> uploaded.toString,

      /**
       * The total amount downloaded (since the client sent the 'started' event
       * to the tracker) in base ten ASCII. While not explicitly stated in the
       * official specification, the consensus is that this should be the total
       * number of bytes downloaded.
       */
      "downloaded" -> downloaded.toString,

      /**
       * The number of bytes this client still has to download in base ten
       * ASCII. Clarification: The number of bytes needed to download to be 100%
       * complete and get all the included files in the torrent.
       */
      "left" -> (metainfo.fileInfo.totalBytes - downloaded).toString,

      /**
       * Setting this to 1 indicates that the client accepts a compact response.
       * The peers list is replaced by a peers string with 6 bytes per peer. The
       * first four bytes are the host (in network byte order), the last two
       * bytes are the port (again in network byte order). It should be noted
       * that some trackers only support compact responses (for saving bandwidth)
       * and either refuse requests without "compact=1" or simply send a compact
       * response unless the request contains "compact=0" (in which case they
       * will refuse the request.)
       */
      "compact" -> "1",

      /**
       * Indicates that the tracker can omit peer id field in peers dictionary.
       * This option is ignored if compact is enabled.
       */
      "no_peer_id" -> "1",

      /**
       * If specified, must be one of started, completed, stopped, (or empty
       * which is the same as not being specified). If not specified, then this
       * request is one performed at regular intervals.
       * - started: The first request to the tracker must include the event key
       *    with this value.
       * - stopped: Must be sent to the tracker if the client is shutting down
       *    gracefully.
       * - completed: Must be sent to the tracker when the download completes.
       *    However, must not be sent if the download was already 100% complete
       *    when the client started. Presumably, this is to allow the tracker to
       *    increment the "completed downloads" metric based solely on this event.
       */
      "event" -> eventStr,

      /**
       * Optional. The true IP address of the client machine, in dotted quad
       * format or rfc3513 defined hexed IPv6 address. Notes: In general this
       * parameter is not necessary as the address of the client can be
       * determined from the IP address from which the HTTP request came. The
       * parameter is only needed in the case where the IP address that the
       * request came in on is not the IP address of the client. This happens if
       * the client is communicating to the tracker through a proxy (or a
       * transparent web proxy/cache.) It also is necessary when both the client
       * and the tracker are on the same local side of a NAT gateway. The reason
       * for this is that otherwise the tracker would give out the internal
       * (RFC1918) address of the client, which is not routable. Therefore the
       * client must explicitly state its (external, routable) IP address to be
       * given out to external peers. Various trackers treat this parameter
       * differently. Some only honor it only if the IP address that the request
       * came in on is in RFC1918 space. Others honor it unconditionally, while
       * others ignore it completely. In case of IPv6 address (e.g.:
       * 2001:db8:1:2::100) it indicates only that client can communicate via
       * IPv6.
       */
      // "ip" -> "",

      /**
       * Optional. Number of peers that the client would like to receive from
       * the tracker. This value is permitted to be zero. If omitted, typically
       * defaults to 50 peers.
       */
      "numwant" -> "50"

      /**
       * Optional. An additional client identification mechanism that is not
       * shared with any peers. It is intended to allow a client to prove their
       * identity should their IP address change.
       */
      // "key" -> "",

      /**
       * Optional. If a previous announce contained a tracker id, it should be
       * set here.
       */
      // "trackerid" -> ""
    )
  }
}
