name := "scala-torrent"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.4"

libraryDependencies ++= {
  val akkaV = "2.3.7"
  Seq(
    "com.typesafe.akka"      %% "akka-actor"               % akkaV,
    "com.typesafe.akka"      %% "akka-testkit"             % akkaV,
    "io.spray"               %  "spray-client_2.11"        % "1.3.2",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2",
    "com.typesafe"           %  "config"                   % "1.2.1",
    "org.scalatest"          %  "scalatest_2.11"           % "2.2.2" % "test"
  )
}