import sbt._
import Keys._
import Dependencies._
import com.typesafe.sbtosgi.OsgiPlugin._

object Resolvers {
  
  val eknet = "eknet.org" at "http://maven.eknet.org/repo"
  
}

object Dependencies {

  val osgiCore = "org.osgi" % "org.osgi.core" % "4.3"
  val slf4jApi = "org.slf4j" % "slf4j-api" % "1.6.4"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.0.1"
  val servletApi = "javax.servlet" % "servlet-api" % "2.5" % "provided"
  val knockoff = "com.tristanhunt" %% "knockoff" % "0.8.0-16" //scala 2.9.1 only
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
  val scalaTest = "org.scalatest" %% "scalatest" % "1.8.RC1" % "test"
  val grizzledSlf4j = "org.clapper" %% "grizzled-slf4j" % "0.6.8" //scala 2.9.1 only
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
    name := "publet-parent",
    libraryDependencies ++= deps
  ) 

  override lazy val settings = super.settings ++ Seq(
    version := "1.0.0-SNAPSHOT",
    scalaVersion := globalScalaVersion,
    sbtPlugin := true,
    resolvers := Seq(Resolvers.eknet),
    scalacOptions ++= Seq("-unchecked", "-deprecation")
  )

  lazy val deps = Seq(osgiCore)

}


// Sub Modules

object Publet extends Build {

  lazy val module = Project(
    id = "publet", 
    base = file("publet"),  
    settings = buildSettings
  )
  
  lazy val buildSettings = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet",
    OsgiKeys.exportPackage := Seq("org.eknet.publet"),
    libraryDependencies ++= deps
  ) ++ osgiSettings
  
  lazy val deps = Seq(knockoff, slf4jApi, grizzledSlf4j, scalaTest)

}

object GitPartition extends Build {

  lazy val module = Project(
    id = "git-partition", 
    base = file("git-partition"),
    settings = buildSettings
  ) dependsOn (Publet.module, Auth.module)

  val buildSettings = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "git-partition",
    description := "Provides a partition for publet around jgit.",
    libraryDependencies ++= deps
  ) ++ osgiSettings 

  val deps = Seq(slf4jApi, jgit, shiro, grizzledSlf4j, scalaTest)

  OsgiKeys.exportPackage := Seq("org.eknet.publet.gitr", "org.eknet.publet.partition.git")
}

object ScalaScriptEngine extends Build {

  lazy val module = Project(
    id = "scala-script",
    base = file("scala-script"),
    settings = buildProperties
  ) dependsOn (Publet.module)

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-scala-scriptengine",
    description := "Provides a engine that compiles and executes scala " +
      "scripts, converting from *.scala source files to some dynamic content.",
    libraryDependencies ++= deps
  ) ++ osgiSettings

  val deps = Seq(slf4jApi, scalaCompiler, scalaTest, grizzledSlf4j)

  OsgiKeys.exportPackage := Seq("org.eknet.publet.engine.scala")
}

object Web extends Build {

  lazy val module = Project(
    id = "web", 
    base = file("web"),
    settings = buildProperties
  ) dependsOn (Publet.module, ScalaScriptEngine.module, GitPartition.module, Auth.module)

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-web",
    libraryDependencies ++= deps
  ) ++ osgiSettings

  val deps = Seq(servletApi, 
       slf4jApi, grizzledSlf4j,
       commonsFileUpload,
       commonsIo, 
       jgitHttpServer,
       shiro, shiroWeb,
       scalaTest)

  OsgiKeys.exportPackage := Seq("org.eknet.publet.web")
}

object War extends Build {

  lazy val module = Project(
    id = "war",
    base = file("war"),
    settings = buildProperties
  ) dependsOn (Publet.module, GitPartition.module, Web.module, WebEditor.module, Ext.module)

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
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

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-auth",
    description := "Authentication module for publet, exposing a AuthManager and default impls.",
    libraryDependencies ++= deps
  ) ++ osgiSettings

  OsgiKeys.exportPackage := Seq("org.eknet.publet.auth")

  val deps = Seq(slf4jApi, grizzledSlf4j, scalaTest)
}

object WebEditor extends Build {

  lazy val module = Project(
    id = "webeditor",
    base = file("webeditor"),
    settings = buildProperties
  ) dependsOn (Publet.module, Web.module)

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-webeditor",
    libraryDependencies ++= deps
  ) ++ osgiSettings

  val deps = Seq(servletApi, grizzledSlf4j, scalaTest)

  OsgiKeys.exportPackage := Seq("org.eknet.publet.webeditor")
} 

object Ext extends Build {
  
  lazy val module = Project(
    id = "ext",
    base = file("ext"),
    settings = buildProperties
  ) dependsOn (Publet.module, Web.module)

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-ext",
    libraryDependencies ++= deps
  ) ++ osgiSettings

  val deps = Seq(squareMail, servletApi, grizzledSlf4j, scalaTest)

  OsgiKeys.exportPackage := Seq("org.eknet.publet.ext")
}
