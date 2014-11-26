package com.dominikgruber.scalatorrent.actor

import akka.actor.{ActorRef, Props, Actor}
import akka.io.{IO, Tcp}
import com.dominikgruber.scalatorrent.tracker.{Peer => PeerInformation}
import java.net.InetSocketAddress

object ConnectionHandler {
  case class CreatePeerConnection(peer: PeerInformation)
  case class PeerConnectionCreated(connection: ActorRef, peer: PeerInformation)
}

class ConnectionHandler(endpoint: InetSocketAddress, internalPeerId: String) extends Actor {
  import Tcp._
  import context.system
  import ConnectionHandler._

  // Torrent coordinator actor
  val coordinator = context.parent

  // Start listening to incoming connections
  IO(Tcp) ! Tcp.Bind(self, endpoint)

  override def receive = {
    case CommandFailed(_: Bind) =>
      // TODO: Handle failure

    case c @ Connected(remoteAddress, _) =>
      val handler = createPeerConnectionActor(remoteAddress)
      sender ! Register(handler)

    case CreatePeerConnection(peer) =>
      val peerConnection = createPeerConnectionActor(peer.inetSocketAddress)
      sender ! PeerConnectionCreated(peerConnection, peer)
  }

  private def createPeerConnectionActor(remoteAddress: InetSocketAddress) =
    context.actorOf(Props(classOf[PeerConnection], remoteAddress, internalPeerId, coordinator), "peer-connection-" + remoteAddress.toString.replace("/", ""))

  /**
   * @return The port for connections
   */
  def port: Int = endpoint.getPort
}