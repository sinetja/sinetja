organization := "tv.cntt"

name         := "sinetja"

version      := "1.2-SNAPSHOT"

//------------------------------------------------------------------------------

scalaVersion := "2.11.2"

autoScalaLibrary := false

// Do not append Scala versions to the generated artifacts
crossPaths := false

// Netty 4+ requires Java 6
javacOptions in (Compile) ++= Seq("-source", "1.6", "-target", "1.6", "-Xlint:deprecation")

javacOptions in (Compile, doc) := Seq("-source", "1.6")

//------------------------------------------------------------------------------

// Projects using Sinetja must provide an implementation of SLF4J (Logback etc.)
libraryDependencies += "org.slf4j" % "slf4j-api" % "1.7.7" % "provided"

libraryDependencies += "io.netty" % "netty-all" % "4.0.24.Final"

// Netty speed can be boosted by Javassist
libraryDependencies += "org.javassist" % "javassist" % "3.18.2-GA"

libraryDependencies += "tv.cntt" % "netty-router" % "1.10"

libraryDependencies += "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.3"
