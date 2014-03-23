name := "scala-torrent"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.4"

libraryDependencies ++= {
  val akkaV = "2.3.0"
  Seq(
    "com.typesafe.akka"   %%  "akka-actor"        % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"      % akkaV,
    "org.scalatest"       %   "scalatest_2.10"    % "2.1.2" % "test"
  )
}