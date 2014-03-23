package com.dominikgruber.scalatorrent

import akka.actor.{Props, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.dominikgruber.scalatorrent.actor.TorrentCoordinator
import com.dominikgruber.scalatorrent.actor.TorrentCoordinator._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.Success

object Boot extends App {

  println("Starting scala-torrent...")

  // Start actor system and coordinator actor
  implicit val system = ActorSystem("scala-torrent")
  val coordinator = system.actorOf(Props(classOf[TorrentCoordinator]), "coordinator")

  // Listen for commands
  print("> ")
  Iterator.continually(Console.readLine()).foreach { cmd =>
    cmd match {
      case _ if cmd.startsWith("add") =>
        addTorrentFiles(cmd.substring(3).split(' ').filter(_.trim != "").toList)
      case "help" => printHelp()
      case "quit" => quit()
      case _ if cmd != "" =>
        println("Unknown command. Type 'help' for a list of all commands.")
      case _ =>
    }
    print("> ")
  }

  def addTorrentFiles(files: List[String]) = {
    if (files.isEmpty)
      println("No files specified. See 'help' for further instructions.")
    else {
      implicit val timeout = Timeout(5 seconds)
      files.foreach { file =>
        (coordinator ? AddTorrentFile(file)) onComplete {
          case Success(TorrentAddedSuccessfully(file1)) =>
            print(s"\r$file1 added successfully.\n> ")
          case Success(TorrentFileInvalid(file1, message)) =>
            print(s"\rFailed to add $file1: $message\n> ")
          case _ =>
        }
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