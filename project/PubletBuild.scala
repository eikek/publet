import sbt._
import Keys._

object Resolvers {
  
  val eknet = "eknet.org" at "http://maven.eknet.org"
  
}

object PubletBuild extends Build {

  lazy val root = Project(id = "publet", 
    base = file("."),
    settings = Project.defaultSettings ++ buildSettings
  )  aggregate (PubletWebBuild.web)

  val buildSettings = Seq(
    name := "publet",
    libraryDependencies ++= commonDeps
  )

  override lazy val settings = super.settings ++ Seq(
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.9.1",
    sbtPlugin := true,
    resolvers := Seq(Resolvers.eknet)
  )

  val commonDeps = Seq(
    "org.slf4j" % "slf4j-api" % "1.6.4"
  )
}


