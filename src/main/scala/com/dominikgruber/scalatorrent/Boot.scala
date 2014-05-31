package com.dominikgruber.scalatorrent

import akka.actor.{Props, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.dominikgruber.scalatorrent.actor.Coordinator
import com.dominikgruber.scalatorrent.actor.Coordinator._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

object Boot extends App {

  println("Starting scala-torrent...")

  // Start actor system and coordinator actor
  implicit val system = ActorSystem("scala-torrent")
  val coordinator = system.actorOf(Props(classOf[Coordinator]), "coordinator")

  // Listen for commands
  print("> ")
  Iterator.continually(Console.readLine()).foreach { cmd =>
    cmd match {
      case _ if cmd.startsWith("add ") => addTorrentFile(cmd.substring(4).trim)
      case "help" => printHelp()
      case "quit" => quit()
      case "exit" => quit()
      case _ if cmd != "" =>
        println("Unknown command. Type 'help' for a list of all commands.")
      case _ =>
    }
  }

  def addTorrentFile(file: String) = {
    if (file.isEmpty)
      println("No file specified. See 'help' for further instructions.")
    else {
      implicit val timeout = Timeout(5 seconds)
      (coordinator ? AddTorrentFile(file)) onComplete {
        case Success(TorrentAddedSuccessfully(file1)) =>
          print(s"\r$file1 added successfully.\n> ")
        case Success(TorrentFileInvalid(file1, message)) =>
          print(s"\rFailed to add $file1: $message\n> ")
        case _ =>
      }
    }
  }

  def printHelp() = {
    println("TODO")
  }

  def quit() = {
    println("Shutting down scala-torrent...")
    system.shutdown()
    System.exit(0)
  }
}