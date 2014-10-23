organization := "tv.cntt"

name         := "sinetja"

version      := "1.0-SNAPSHOT"

//------------------------------------------------------------------------------

scalaVersion := "2.11.2"

autoScalaLibrary := false

// Do not append Scala versions to the generated artifacts
crossPaths := false

javacOptions in (Compile) ++= Seq("-source", "1.5", "-target", "1.5", "-Xlint:deprecation")

javacOptions in (Compile, doc) := Seq("-source", "1.5")

//------------------------------------------------------------------------------

// Projects using Sinetja must provide a concrete implementation of SLF4J (Logback etc.)
libraryDependencies += "org.slf4s" %% "slf4s-api" % "1.7.7"

libraryDependencies += "io.netty" % "netty-all" % "4.0.23.Final"

// Netty speed can be boosted by Javassist
libraryDependencies += "org.javassist" % "javassist" % "3.18.2-GA"

libraryDependencies += "tv.cntt" % "jauter" % "1.3-SNAPSHOT"
