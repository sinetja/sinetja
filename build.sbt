organization := "tv.cntt"
name         := "sinetja"
version      := "1.4.0-SNAPSHOT"

//------------------------------------------------------------------------------

// Scala is only used for SBT as a build tool
scalaVersion := "2.12.2"
autoScalaLibrary := false

// Do not append Scala versions to the generated artifacts
crossPaths := false

// Netty 4+ requires Java 6
javacOptions in Compile ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:deprecation")
javacOptions in (Compile, doc) := Seq("-source", "1.6")

//------------------------------------------------------------------------------

// Projects using Sinetja must provide an implementation of SLF4J (Logback etc.)
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.25" % "provided"

libraryDependencies += "io.netty" % "netty-all" % "4.1.11.Final"

libraryDependencies += "tv.cntt" % "netty-router" % "2.2.0"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.8.1"
