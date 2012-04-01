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
    name := "publet-root"
  )

  override lazy val settings = super.settings ++ Seq(
    version := "1.0.0-SNAPSHOT",
    scalaVersion := "2.9.1",
    sbtPlugin := true,
    resolvers := Seq(Resolvers.eknet)
  )
}

object Dependencies {

  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.6.4"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.0.1"
  val servletApi = "javax.servlet" % "servlet-api" % "2.5" % "provided"
  val knockoff = "com.tristanhunt" %% "knockoff" % "0.8.0-16"
  val jettyContainer = "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
  val commonsFileUpload = "commons-fileupload" % "commons-fileupload" % "1.2.2"
  val commonsIo = "commons-io" % "commons-io" % "2.2"
//  val commonsLang = "org.apache.commons" % "commons-lang3" % "3.1"
}


