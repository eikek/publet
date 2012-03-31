import sbt._
import Keys._

object Resolvers {
  
  val eknet = "eknet.org" at "http://maven.eknet.org"
  
}

object RootBuild extends Build {

  lazy val root = Project(id = "publet-root",
    base = file("."),
    settings = buildSettings
  ) aggregate (WebBuild.web, PubletBuild.publet)
 
  val buildSettings = Project.defaultSettings ++ Seq(
    name := "publet-root",
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

object Dependencies {

  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.6.4"
  val servletApi = "javax.servlet" % "servlet-api" % "2.5"
  val knockoff = "com.tristanhunt" %% "knockoff" % "0.8.0-16"
  val jettyContainer = "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
  val commonsLang = "org.apache.commons" % "commons-lang3" % "3.1"
}


