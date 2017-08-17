name := "scala-torrent"

version := "0.1-SNAPSHOT"

scalaVersion := "2.11.4" //TODO 2.12

libraryDependencies ++= {
  val akkaV = "2.5.3"
  Seq(
    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "io.spray" %% "spray-client" % "1.3.2",
    "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.2",
    "com.typesafe" % "config" % "1.2.1",
    "com.typesafe.akka" %% "akka-slf4j" % "2.5.4",
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.akka" %% "akka-testkit" % akkaV % Test,
    "org.scalatest" %% "scalatest" % "2.2.2" % Test,
    "org.scalamock" %% "scalamock-core" % "3.1.1" % Test,
    "org.scalamock" %% "scalamock-scalatest-support" % "3.1.1" % Test,
    "org.mockito" % "mockito-all" % "1.10.19" % Test
  )
}
