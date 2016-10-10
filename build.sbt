import sbt.Keys._

scalaVersion := "2.11.6"

name := "mailru-segment-exporter"

version := "1.4.12-SNAPSHOT"


organization := "net.facetz"

organizationName := "FACETz"

organizationHomepage := Some(url("http://facetz.net/"))


libraryDependencies ++= Seq(
  "org.scala-lang" % "scala-reflect" % scalaVersion.value,
  "org.scalaj" %% "scalaj-http" % "1.1.4",
  "com.github.scopt" %% "scopt" % "3.3.0",
  "io.argonaut" %% "argonaut" % "6.1",
  "commons-io" % "commons-io" % "2.4",
  "org.apache.commons" % "commons-lang3" % "3.3.2",
  "joda-time" % "joda-time" % "2.8.2",
  "org.scalatest" %% "scalatest" % "3.0.0" % "test"
)

mainClass in assembly := Some("net.facetz.mailru.Runner")

assemblyJarName in assembly := s"${name.value}_${scalaBinaryVersion.value}-${version.value}.jar"

artifact in(Compile, assembly) := {
  val art = (artifact in(Compile, assembly)).value
  art.copy(`classifier` = Some("assembly"))
}

addArtifact(artifact in(Compile, assembly), assembly).settings

// compile task depends on scalastyle
lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")

compileScalastyle := org.scalastyle.sbt.ScalastylePlugin.scalastyle.in(Compile).toTask("").value

(compile in Compile) <<= (compile in Compile) dependsOn compileScalastyle