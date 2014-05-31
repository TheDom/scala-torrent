name := "scala-torrent"

version := "0.1-SNAPSHOT"

scalaVersion := "2.10.4"

libraryDependencies ++= {
  val akkaV = "2.3.3"
  Seq(
    "com.typesafe.akka"   %%  "akka-actor"        % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"      % akkaV,
    "io.spray"            %   "spray-client"      % "1.3.1",
    "org.scalatest"       %   "scalatest_2.10"    % "2.1.7" % "test"
  )
}