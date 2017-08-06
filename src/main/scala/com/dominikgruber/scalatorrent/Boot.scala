package com.dominikgruber.scalatorrent

import akka.actor.{Props, ActorSystem}
import akka.pattern.ask
import akka.util.Timeout
import com.dominikgruber.scalatorrent.actor.Coordinator
import com.dominikgruber.scalatorrent.actor.Coordinator._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn
import scala.language.postfixOps
import scala.util.Success

object Boot extends App {

  println("")
  println("                    __            __                             __")
  println("   ______________ _/ /___ _      / /_____  _____________  ____  / /_")
  println("  / ___/ ___/ __ `/ / __ `/_____/ __/ __ \\/ ___/ ___/ _ \\/ __ \\/ __/")
  println(" (__  ) /__/ /_/ / / /_/ /_____/ /_/ /_/ / /  / /  /  __/ / / / /_")
  println("/____/\\___/\\__,_/_/\\__,_/      \\__/\\____/_/  /_/   \\___/_/ /_/\\__/")
  println("")
  println("")

  // Start actor system and coordinator actor
  implicit val system = ActorSystem("scala-torrent")
  val coordinator = system.actorOf(Props(classOf[Coordinator]), "coordinator")

  // TMP
  addTorrentFile("/Users/victorbasso/Downloads/ubuntu-12.04.5-desktop-amd64.iso.torrent")
//  addTorrentFile("/Users/victorbasso/Downloads/no_checksums.torrent")
  // TMP

  // Listen for commands
  print("> ")
  Iterator.continually(StdIn.readLine()).foreach { cmd =>
    cmd match {
      case _ if cmd.startsWith("add ") => addTorrentFile(cmd.substring(4).trim)
      case "help" => printHelp()
      case "quit" => quit()
      case "exit" => quit()
      case _ if !cmd.trim.isEmpty =>
        println("Unknown command. Type 'help' for a list of all commands.")
      case _ =>
    }
    print("> ")
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
    println("add <path>     Add a torrent file")
    println("quit           Quit the client")
  }

  def quit() = {
    println("Shutting down scala-torrent...")
    // TODO: Notify coordinator and wait for ACK (connections need to be properly closed)
    system.terminate().onComplete {
      _ => System.exit(0)
    }
  }
}