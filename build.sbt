scalaVersion := "2.11.6"

name := "mailru-segment-exporter"

version := "1.0"


organization := "net.facetz"

organizationName := "FACETz"

organizationHomepage := Some(url("http://facetz.net/"))


libraryDependencies +=  "org.scalaj" %% "scalaj-http" % "1.1.4"

libraryDependencies += "com.github.scopt" %% "scopt" % "3.3.0"

libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.10.0"

libraryDependencies += "io.argonaut" %% "argonaut" % "6.0.4"

libraryDependencies += "commons-io" % "commons-io" % "2.4"

libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.3.2"

mainClass in assembly := Some("net.facetz.mailru.Runner")
