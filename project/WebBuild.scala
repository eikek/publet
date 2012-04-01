import sbt._
import Keys._
import BundlePlugin._
import Dependencies._

object WebBuild extends Build {

  lazy val web = Project(id = "publet-web", 
    base = file("publet-web"),
    settings = buildProperties
  ) dependsOn PubletBuild.publet

  val buildProperties = Project.defaultSettings ++ Seq(
    name := "publet-web",
    libraryDependencies ++= deps
  ) ++ bundleSettings

  val deps = Seq(servletApi, slf4jApi, logbackClassic, commonsFileUpload, commonsIo)
}
