import sbt._
import Keys._
import Dependencies._
import com.typesafe.sbtosgi.OsgiPlugin._
import com.github.siasia._
import WebappPlugin.webappSettings

object Version {
  val osgi = "4.3"
  val slf4j = "1.6.4"
  val logback = "1.0.1"
  val servlet = "2.5"
  val cfileupload = "1.2.2"
  val cio = "2.2"
  val squaremail = "1.0.0"
  val jgit = "1.3.0.201202151440-r"
  val shiro = "1.2.0"
  val scalaTest = "1.8"
  val grizzled = "0.6.8"
  val scalate = "1.5.3"
  val mimeUtil = "2.1.3"
  val orientdb = "1.0.1"
  val blueprints = "2.0.0"
  val milton = "1.8.0.1"
  val ccodec = "1.4"
  val jdom = "1.1"
}

object Dependencies {

  val osgiCore = "org.osgi" % "org.osgi.core" % Version.osgi withSources()
  val slf4jApi = "org.slf4j" % "slf4j-api" % Version.slf4j
  val logbackClassic = "ch.qos.logback" % "logback-classic" % Version.logback withSources()
  val servletApi = "javax.servlet" % "servlet-api" % Version.servlet % "provided" withSources()
  val jettyContainer = "org.eclipse.jetty" % "jetty-webapp" % "8.0.1.v20110908" % "container" withSources()
  val commonsFileUpload = "commons-fileupload" % "commons-fileupload" % Version.cfileupload
  val commonsIo = "commons-io" % "commons-io" % Version.cio withSources()
  val scalaCompiler = "org.scala-lang" % "scala-compiler" % RootBuild.globalScalaVersion withSources()
  val squareMail = "org.eknet.squaremail" % "squaremail" % Version.squaremail
  val jgit = "org.eclipse.jgit" % "org.eclipse.jgit" % Version.jgit withSources()
  val jgitHttpServer = "org.eclipse.jgit" % "org.eclipse.jgit.http.server" % Version.jgit withSources()
  val shiro = "org.apache.shiro" % "shiro-core" % Version.shiro withSources()
  val shiroWeb = "org.apache.shiro" % "shiro-web" % Version.shiro withSources()
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % "test" withSources()
  val grizzledSlf4j = "org.clapper" %% "grizzled-slf4j" % Version.grizzled withSources() //scala 2.9.1 only

  val scalateWikitext = "org.fusesource.scalate" % "scalate-wikitext" % Version.scalate
  val scalateCore = "org.fusesource.scalate" % "scalate-core" % Version.scalate
  val scalatePage = "org.fusesource.scalate" % "scalate-page" % Version.scalate

  val mimeUtil = "eu.medsea.mimeutil" % "mime-util" % Version.mimeUtil intransitive()

  val orientdbCore = "com.orientechnologies" % "orientdb-core" % Version.orientdb withSources()
  val orientCommons = "com.orientechnologies" % "orient-commons" % Version.orientdb withSources()
  val blueprintsCore = "com.tinkerpop.blueprints" % "blueprints-core" % Version.blueprints withSources() intransitive()
  val blueprints = "com.tinkerpop.blueprints" % "blueprints-orient-graph" % Version.blueprints withSources() intransitive() //uses orientdb 1.0.1

  val miltonApi = "com.ettrema" % "milton-api" % Version.milton withSources() intransitive() from("http://www.ettrema.com/maven2")
  val miltonApiDeps = Seq(
    "commons-codec" % "commons-codec" % Version.ccodec withSources(),
    "org.jdom" % "jdom" % Version.jdom
  )
  val miltonServlet = "com.ettrema" % "milton-servlet" % Version.milton withSources() intransitive() from("http://www.ettrema.com/maven2")

}

// Root Module 

object RootBuild extends Build {
  lazy val container = Container("container")

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = buildSettings
  ) aggregate (
      Publet.module,
      Web.module,
      GitPart.module,
      GitrWeb.module,
      Auth.module,
      ScalateEngine.module,
      War.module,
      WebEditor.module,
      Ext.module,
      Doc.module
    )

  val globalScalaVersion = "2.9.1"

  val buildSettings = Project.defaultSettings ++ Seq(
    name := "publet-parent",
    libraryDependencies ++= deps
  ) ++ container.deploy("/publet" -> War.module) ++ Seq(PluginKeys.port in container.Configuration := 8081)

  override lazy val settings = super.settings ++ Seq(
    version := "1.0.0-SNAPSHOT",
    scalaVersion := globalScalaVersion,
    sbtPlugin := true,
    exportJars := true,
    scalacOptions ++= Seq("-unchecked", "-deprecation")
  )

  lazy val deps = Seq(jettyContainer)

}


// Sub Modules

object Gitr extends Build {

  lazy val module = Project(
    id = "gitr",
    base = file("gitr"),
    settings = buildSettings
  )

  lazy val buildSettings = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-gitr",
    libraryDependencies ++= deps
  ) ++ osgiSettings

  lazy val deps = Seq(slf4jApi, jgit, grizzledSlf4j, scalaTest)

}

object GitrWeb extends Build {

  lazy val module = Project(
    id = "gitr-web",
    base = file("gitr-web"),
    settings = buildSettings
  ) dependsOn(Gitr.module, Web.module)

  lazy val buildSettings = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-gitr-web",
    libraryDependencies ++= deps
  ) ++ osgiSettings

  lazy val deps = Seq(grizzledSlf4j, servletApi, scalaTest)

}


object Publet extends Build {

  lazy val module = Project(
    id = "publet", 
    base = file("publet"),  
    settings = buildSettings
  )
  
  lazy val buildSettings = Project.defaultSettings ++ ReflectPlugin.allSettings ++ Seq[Project.Setting[_]](
    name := "publet",
    OsgiKeys.exportPackage := Seq("org.eknet.publet"),
    libraryDependencies ++= deps,
    ReflectPlugin.reflectPackage := "org.eknet.publet.reflect",
    sourceGenerators in Compile <+= ReflectPlugin.reflect
  ) ++ osgiSettings
  
  lazy val deps = Seq(slf4jApi, grizzledSlf4j, mimeUtil, scalaTest)

}

object GitPart extends Build {

  lazy val module = Project(
    id = "git-part", 
    base = file("git-part"),
    settings = buildSettings
  ) dependsOn (Publet.module, Auth.module, Gitr.module)

  val buildSettings = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-git-part",
    description := "Provides a partition for publet around jgit.",
    libraryDependencies ++= deps
  ) ++ osgiSettings 

  val deps = Seq(slf4jApi, shiro, grizzledSlf4j, scalaTest)

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
  ) dependsOn (Publet.module, ScalaScriptEngine.module, ScalateEngine.module, GitPart.module, Auth.module)

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
       miltonApi, miltonServlet,
       scalaTest) ++ miltonApiDeps

  OsgiKeys.exportPackage := Seq("org.eknet.publet.web")
}

object War extends Build {

  lazy val module = Project(
    id = "war",
    base = file("war"),
    settings = buildProperties
  ) dependsOn (Publet.module, GitPart.module, ScalateEngine.module, Web.module,
    WebEditor.module, Ext.module, GitrWeb.module, Doc.module)

  val buildProperties = Project.defaultSettings ++ webappSettings ++ Seq[Project.Setting[_]](
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

  val deps = Seq(squareMail, servletApi, grizzledSlf4j, scalaTest, blueprints, blueprintsCore, orientdbCore, orientCommons)

  OsgiKeys.exportPackage := Seq("org.eknet.publet.ext")
}

object ScalateEngine extends Build {

  lazy val module = Project(
    id = "scalate",
    base = file("scalate"),
    settings = buildProperties
  ) dependsOn Publet.module

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-scalate",
    libraryDependencies ++= deps
  ) ++ osgiSettings

  val deps = Seq(slf4jApi, grizzledSlf4j, scalateCore, scalateWikitext, scalatePage)
}

object Doc extends Build {

  lazy val module = Project(
    id = "doc",
    base = file("doc"),
    settings = buildProperties
  ) dependsOn Web.module

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-doc",
    libraryDependencies ++= deps
  ) ++ osgiSettings

  val deps = Seq(grizzledSlf4j)
}
