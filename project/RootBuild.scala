
import sbt._
import Keys._
import BundlePlugin._
import Dependencies._
import sbt.Build

object Resolvers {
  
  val eknet = "eknet.org" at "http://maven.eknet.org/repo"
  
}

object Dependencies {

  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.6.4"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.0.1"
  val servletApi = "javax.servlet" % "servlet-api" % "2.5" % "provided"
  val knockoff = "com.tristanhunt" %% "knockoff" % "0.8.0-16"
  val jettyContainer = "org.mortbay.jetty" % "jetty" % "6.1.22" % "container"
  val commonsFileUpload = "commons-fileupload" % "commons-fileupload" % "1.2.2"
  val commonsIo = "commons-io" % "commons-io" % "2.2"
  val scalascriptengine = "com.googlecode.scalascriptengine" % "scalascriptengine" % "0.6.4"
  val scalaCompiler = "org.scala-lang" % "scala-compiler" % RootBuild.globalScalaVersion
  val squareMail = "org.eknet.squaremail" % "squaremail" % "1.0.0"
  val jgit = "org.eclipse.jgit" % "org.eclipse.jgit" % "1.3.0.201202151440-r"
  val jgitHttpServer = "org.eclipse.jgit" % "org.eclipse.jgit.http.server" % "1.3.0.201202151440-r"
  val shiro = "org.apache.shiro" % "shiro-core" % "1.2.0"
  val shiroWeb = "org.apache.shiro" % "shiro-web" % "1.2.0"

}

// Root Module 

object RootBuild extends Build {

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = buildSettings
  ) aggregate (Web.module, Publet.module, GitPartition.module, Auth.module, War.module, WebEditor.module, Ext.module)

  val globalScalaVersion = "2.9.1"

  val buildSettings = Project.defaultSettings ++ Seq(
    name := "publet-parent"
  )

  override lazy val settings = super.settings ++ Seq(
    version := "1.0.0-SNAPSHOT",
    scalaVersion := globalScalaVersion,
    sbtPlugin := true,
    resolvers := Seq(Resolvers.eknet),
    scalacOptions ++= Seq("-unchecked", "-deprecation")
  )
}


// Sub Modules

object Publet extends Build {

  lazy val module = Project(
    id = "publet", 
    base = file("publet"),  
    settings = buildSettings
  )
  
  lazy val buildSettings = Project.defaultSettings ++ Seq(name := "publet",
    libraryDependencies ++= deps
  ) ++ bundleSettings 
  
  lazy val deps = Seq(knockoff, slf4jApi, scalaCompiler )
}

object GitPartition extends Build {

  lazy val module = Project(
    id = "git-partition", 
    base = file("git-partition"),
    settings = buildProperties
  ) dependsOn Publet.module

  val buildProperties = Project.defaultSettings ++ Seq(
    name := "git-partition",
    libraryDependencies ++= deps
  ) ++ bundleSettings

  val deps = Seq(slf4jApi, jgit)
}

object Web extends Build {

  lazy val module = Project(
    id = "web", 
    base = file("web"),
    settings = buildProperties
  ) dependsOn (Publet.module, GitPartition.module)

  val buildProperties = Project.defaultSettings ++ Seq(
    name := "publet-web",
    libraryDependencies ++= deps
  ) ++ bundleSettings

  val deps = Seq(servletApi, 
       slf4jApi, 
       logbackClassic, 
       commonsFileUpload, 
       commonsIo, 
       squareMail, 
       jgitHttpServer, 
       shiro, shiroWeb)
}

object War extends Build {

  lazy val module = Project(
    id = "war",
    base = file("war"),
    settings = buildProperties
  ) dependsOn (Publet.module, GitPartition.module, Web.module)

  val buildProperties = Project.defaultSettings ++ Seq(
    name := "publet-war",
    libraryDependencies ++= deps
  )

  val deps = Seq(servletApi, logbackClassic)
}

object Auth extends Build {

  lazy val module = Project(
    id = "auth",
    base = file("auth"),
    settings = buildProperties
  ) dependsOn (Publet.module)

  val buildProperties = Project.defaultSettings ++ Seq(
    name := "publet-auth",
    libraryDependencies ++= deps
  )

  val deps = Seq(slf4jApi)
}

object WebEditor extends Build {

  lazy val module = Project(
    id = "webeditor",
    base = file("webeditor"),
    settings = buildProperties
  ) dependsOn (Publet.module, Web.module)

  val buildProperties = Project.defaultSettings ++ Seq(
    name := "publet-webeditor",
    libraryDependencies ++= deps
  )

  val deps = Seq()
}

object Ext extends Build {
  
  lazy val module = Project(
    id = "ext",
    base = file("ext"),
    settings = buildProperties
  ) dependsOn (Publet.module, Web.module)

  val buildProperties = Project.defaultSettings ++ Seq(
    name := "publet-ext",
    libraryDependencies ++= deps
  )

  val deps = Seq(squareMail)
}
