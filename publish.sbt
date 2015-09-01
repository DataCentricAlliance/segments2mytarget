publishMavenStyle := true

publishTo := {
  val nexus = "http://repodirect:8081/nexus/"
  if (isSnapshot.value) {
    Some("snapshots" at nexus + "content/repositories/snapshots")
  } else {
    Some("releases" at nexus + "content/repositories/thirdparty")
  }
}

/**
 * Example content of ~/.sbt/.credentials :
 *
 * realm=Sonatype Nexus Repository Manager
 * host=repo
 * user=username
 * password=password
 */
credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
