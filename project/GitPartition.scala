import sbt._
import Keys._
import BundlePlugin._
import Dependencies._

object GitPartition extends Build {

  lazy val module = Project(id = "git-partition", 
    base = file("git-partition"),
    settings = buildProperties
  ) dependsOn PubletBuild.publet

  val buildProperties = Project.defaultSettings ++ Seq(
    name := "git-partition",
    libraryDependencies ++= deps
  ) ++ bundleSettings

  val deps = Seq(slf4jApi, jgit)
}
