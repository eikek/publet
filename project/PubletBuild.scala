import sbt._
import Keys._
import BundlePlugin._
import Dependencies._

object PubletBuild extends Build {

  lazy val publet = Project(id = "publet", base = file("publet"), settings = buildSettings)
  
  lazy val buildSettings = Project.defaultSettings ++ Seq(name := "publet",
    libraryDependencies ++= deps
  ) ++ bundleSettings
  
  lazy val deps = Seq(knockoff, slf4jApi, scalaCompiler )
}
