name := """todotxt2mqtt"""
organization := "blaha"

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.3"

val AkkaVersion = "2.5.31"

libraryDependencies += guice
libraryDependencies += "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test

// Alpakka MQTT
libraryDependencies ++= Seq(
  "com.lightbend.akka" %% "akka-stream-alpakka-mqtt" % "2.0.1",
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "blaha.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "blaha.binders._"
