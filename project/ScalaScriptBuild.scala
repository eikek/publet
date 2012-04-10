import sbt._
import Keys._
import BundlePlugin._
import Dependencies._

object ScalaScriptBuild extends Build {

  lazy val scalascript = Project(id = "publet-scalascript", 
    base = file("publet-scalascript"),
    settings = buildProperties
  ) dependsOn PubletBuild.publet

  val buildProperties = Project.defaultSettings ++ Seq(
    name := "publet-scalascript",
    libraryDependencies ++= deps
  ) ++ bundleSettings

  val deps = Seq(slf4jApi, scalaCompiler, scalascriptengine)
}
