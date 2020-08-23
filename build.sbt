name := """todotxt2mqtt"""
organization := "blaha"

javaOptions in Universal ++= Seq(
  "-Dpidfile.path=/dev/null"
)

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.3"

val AkkaVersion = "2.6.5"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % Test
libraryDependencies += "com.typesafe.akka" %% "akka-testkit" % "2.6.5" % Test
libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.2"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.2" % "test"
libraryDependencies += "org.scalatest" %% "scalatest-mustmatchers" % "3.2.0-M1" % Test

// Alpakka MQTT
libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-mqtt" % "2.0.1",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion
)

//javaOptions in Test += "-Dconfig.file=conf/test.conf"

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "blaha.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "blaha.binders._"