import sbt.Keys._

scalaVersion := "2.11.6"

name := "mailru-segment-exporter"

version := "1.4.1-SNAPSHOT"


organization := "net.facetz"

organizationName := "FACETz"

organizationHomepage := Some(url("http://facetz.net/"))


libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scalaj" %% "scalaj-http" % "1.1.4",
  "com.github.scopt" %% "scopt" % "3.3.0",
  "io.argonaut" %% "argonaut" % "6.0.4",
  "commons-io" % "commons-io" % "2.4",
  "org.apache.commons" % "commons-lang3" % "3.3.2"
)

mainClass in assembly := Some("net.facetz.mailru.Runner")

assemblyJarName in assembly := s"${name.value}_${scalaBinaryVersion.value}-${version.value}.jar"
