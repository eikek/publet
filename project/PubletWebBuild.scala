import sbt._
import Keys._
import BundlePlugin._

object PubletWebBuild extends Build {
  lazy val web = Project(id = "publet-web", 
    base = file("publet-web"),
    settings = buildProperties
  )

  val buildProperties = Project.defaultSettings ++ Seq(
    name := "publet-web",
    libraryDependencies ++= deps
  ) ++ bundleSettings
  
  val deps = Seq(
    "javax.servlet" % "servlet-api" % "2.5"
  )
}
