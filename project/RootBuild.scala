import sbt._
import Keys._
import Dependencies._

object Resolvers {
  val eknet = "eknet.org" at "https://eknet.org/maven2"
  val ettrema = "milton.io" at "http://milton.io/maven"
}
object Version {
  val blueprints = "2.2.0"
  val bouncyCastle = "1.46"
  val ccodec = "1.7"
  val ccollections = "3.2.1"
  val cfileupload = "1.2.2"
  val cio = "2.2"
  val colt = "1.2.0"
  val findbugs = "1.3.9" //required for guava: https://groups.google.com/d/topic/guava-discuss/LV0oLNFpnAU/discussion
  val jdom = "1.1"
  val jgit = "2.1.0.201209190230-r"
  val jetty = "8.1.8.v20121106"
  val googleClosureCompiler = "rr2079.1"
  val grizzled = "0.6.9"
  val guava = "13.0.1"
  val guice = "3.0"
  val logback = "1.0.9"
  val milton = "2.2.3"
  val mimeUtil = "2.1.3"
  val orientdb = "1.3.0"
  val scalate = "1.6.1"
  val scalaTest = "2.0.M6-SNAP3"
  val scue = "0.2.0"
  val servlet = "3.0.1"
  val shiro = "1.2.1"
  val slf4j = "1.7.2"
  val squaremail = "1.0.2"
  val scala = "2.9.2"
  val yuicompressor = "2.4.7"
}

object Dependencies {
  val commonsFileUpload = "commons-fileupload" % "commons-fileupload" % Version.cfileupload
  val commonsIo = "commons-io" % "commons-io" % Version.cio
  val blueprintsCore = "com.tinkerpop.blueprints" % "blueprints-core" % Version.blueprints intransitive()
  val blueprintsOrient = "com.tinkerpop.blueprints" % "blueprints-orient-graph" % Version.blueprints intransitive()
  val bouncyCastleProv = "org.bouncycastle" % "bcprov-jdk16" % Version.bouncyCastle
  val bouncyCastleMail = "org.bouncycastle" % "bcmail-jdk16" % Version.bouncyCastle
  val colt = "colt" % "colt" % Version.colt // is used by blueprints-core
  val findbugs = "com.google.code.findbugs" % "jsr305" % Version.findbugs
  val googleClosureCompiler = "com.google.javascript" % "closure-compiler" % Version.googleClosureCompiler intransitive()
  val grizzledSlf4j = "org.clapper" %% "grizzled-slf4j" % Version.grizzled exclude("org.slf4j", "slf4j-api") //scala 2.9.2 only
  val guava = "com.google.guava" % "guava" % Version.guava
  val guice = "com.google.inject" % "guice" % Version.guice exclude("org.sonatype.sisu.inject", "cglib") exclude("org.slf4j", "slf4j-api")
  val cglib = "cglib" % "cglib" % "2.2.2"
  val guiceServlet = "com.google.inject.extensions" % "guice-servlet" % Version.guice
  val guiceMultibindings = "com.google.inject.extensions" % "guice-multibindings" % Version.guice
  val jettyAjp = "org.eclipse.jetty" % "jetty-ajp" % Version.jetty
  val jettyContainer = "org.eclipse.jetty" % "jetty-webapp" % "8.0.1.v20110908" % "container"
  val jettyServer = "org.eclipse.jetty" % "jetty-webapp" % Version.jetty
  val jgit = "org.eclipse.jgit" % "org.eclipse.jgit" % Version.jgit
  val jgitHttpServer = "org.eclipse.jgit" % "org.eclipse.jgit.http.server" % Version.jgit
  val logbackClassic = "ch.qos.logback" % "logback-classic" % Version.logback  exclude("org.slf4j", "slf4j-api")
  val miltonApi = "io.milton" % "milton-api" % Version.milton intransitive()
  val miltonApiDeps = Seq(
    "commons-codec" % "commons-codec" % Version.ccodec  exclude("rhino", "js"),
    "commons-collections" % "commons-collections" % Version.ccollections ,
    "org.jdom" % "jdom" % Version.jdom
  )
  val miltonServlet = "io.milton" % "milton-server-ce" % Version.milton intransitive()
  val mimeUtil = "eu.medsea.mimeutil" % "mime-util" % Version.mimeUtil intransitive()
  val orientdb = "com.orientechnologies" % "orientdb-core" % Version.orientdb
  val scalaCompiler = "org.scala-lang" % "scala-compiler" % Version.scala
  val scalateCore = "org.fusesource.scalate" % "scalate-core_2.9" % Version.scalate exclude("rhino", "js")
  val scalateUtil = "org.fusesource.scalate" % "scalate-util_2.9" % Version.scalate exclude("rhino", "js")
  val scalatePage = "org.fusesource.scalate" % "scalate-page_2.9" % Version.scalate exclude("rhino", "js")
  val scalateWikitext = "org.fusesource.scalate" % "scalate-wikitext_2.9" % Version.scalate exclude("rhino", "js")
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % "test"
  val scue = "org.eknet.scue" %% "scue" % Version.scue
  val servletApi = "javax.servlet" % "javax.servlet-api" % Version.servlet
  val servletApiProvided = servletApi % "provided"
  val shiro = "org.apache.shiro" % "shiro-core" % Version.shiro exclude("org.slf4j", "slf4j-api")
  val shiroWeb = "org.apache.shiro" % "shiro-web" % Version.shiro exclude("org.slf4j", "slf4j-api")
  val slf4jApi = "org.slf4j" % "slf4j-api" % Version.slf4j
  val slf4jJcl = "org.slf4j" % "jcl-over-slf4j" % Version.slf4j
  val squareMail = "org.eknet.squaremail" % "squaremail" % Version.squaremail
  val yuicompressor = "com.yahoo.platform.yui" % "yuicompressor" % Version.yuicompressor exclude("rhino", "js")
}

// Root Module 

object RootBuild extends Build {
  import com.github.siasia._

  lazy val container = Container("container")

  lazy val root = Project(
    id = "root",
    base = file("."),
    settings = buildSettings
  ) aggregate (
      Publet.module,
      Gitr.module,
      GuiceSquire.module,
      ScalaScriptEngine.module,
      ScalateEngine.module,
      Web.module,
      GitrWeb.module,
      Auth.module,
      War.module,
      WebEditor.module,
      Ext.module,
      Server.module,
      Doc.module,
      App.module,
      Webdav.module
    )

  val buildSettings = Project.defaultSettings ++ Seq(
    name := "publet-parent",
    libraryDependencies ++= deps
  ) ++ container.deploy("/" -> War.module) ++ Seq(PluginKeys.port in container.Configuration := 8081)  ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

  override lazy val settings = super.settings ++ Seq(
    version := "1.1.0-SNAPSHOT",
    organization := "org.eknet.publet",
    scalaVersion := Version.scala,
    publishTo := Some("eknet-maven2" at "https://eknet.org/maven2"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    exportJars := true,
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    resolvers := Seq(Resolvers.eknet, Resolvers.ettrema),
    licenses := Seq(("ASL2", new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))),

    // see https://jira.codehaus.org/browse/JETTY-1493
    ivyXML := <dependency org="org.eclipse.jetty.orbit" name="javax.servlet" rev="3.0.0.v201112011016">
        <artifact name="javax.servlet" type="orbit" ext="jar"/>
    </dependency>
  )

  lazy val deps = Seq(jettyContainer)

}


// Sub Modules

// libraries: no other module dependencies
object Publet extends Build {

  lazy val module = Project(
    id = "publet",
    base = file("publet"),
    settings = buildSettings
  )

  lazy val buildSettings = Project.defaultSettings ++ ReflectPlugin.allSettings ++ Seq[Project.Setting[_]](
    name := "publet",
    libraryDependencies ++= deps,
    ReflectPlugin.reflectPackage := "org.eknet.publet.reflect",
    sourceGenerators in Compile <+= ReflectPlugin.reflect
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

  lazy val deps = Seq(slf4jApi, grizzledSlf4j, mimeUtil, findbugs, guava, scalaTest)

}

object Gitr extends Build {

  lazy val module = Project(
    id = "gitr",
    base = file("gitr"),
    settings = buildSettings
  )

  lazy val buildSettings = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-gitr",
    description := "Thin layer around jgit.",
    libraryDependencies ++= deps
  ) 

  lazy val deps = Seq(slf4jApi, jgit, grizzledSlf4j, scalaTest)

}

object GuiceSquire extends Build {

  lazy val module = Project(
    id = "guice-squire",
    base = file("guice-squire"),
    settings = buildProperties
  )

  val buildProperties = Project.defaultSettings ++ Seq(
    name := "guice-squire",
    description := "Helpers for working with guice using Scala",
    libraryDependencies ++= deps
  )

  val deps = Seq(
    slf4jApi, guice, guiceMultibindings, scalaTest
  )
}

// engine implementations
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
  )

  val deps = Seq(slf4jApi, scalaCompiler, scalaTest, grizzledSlf4j)

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
  )

  val deps = Seq(slf4jApi, grizzledSlf4j, scalateCore, scalateUtil, scalateWikitext, scalatePage)
}

// authentication and authorization module
object Auth extends Build {

  lazy val module = Project(
    id = "auth",
    base = file("auth"),
    settings = buildProperties
  ) dependsOn (Publet.module, GuiceSquire.module)

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-auth",
    description := "Authentication module for publet, exposing a AuthManager and default impls.",
    libraryDependencies ++= deps
  )

  val deps = Seq(slf4jApi, grizzledSlf4j, shiro, guice, scalaTest)
}

// the web module
object Web extends Build {

  lazy val module = Project(
    id = "web",
    base = file("web"),
    settings = buildProperties
  ) dependsOn (Publet.module, ScalaScriptEngine.module, ScalateEngine.module, Auth.module, GuiceSquire.module)

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-web",
    libraryDependencies ++= deps
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

  val deps = Seq(servletApiProvided,
    slf4jApi, grizzledSlf4j,
    commonsFileUpload,
    commonsIo,
    shiroWeb,
    yuicompressor,
    googleClosureCompiler,
    guice,
    cglib,
    guiceServlet,
    scalaTest)

}

// extensions
object GitrWeb extends Build {

  lazy val module = Project(
    id = "gitr-web",
    base = file("gitr-web"),
    settings = buildSettings
  ) dependsOn(Gitr.module, Web.module)

  lazy val buildSettings = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-gitr-web",
    libraryDependencies ++= deps
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

  lazy val deps = Seq(grizzledSlf4j, servletApiProvided, guice, jgitHttpServer, scalaTest)

}

object Webdav extends Build {

  lazy val module = Project(
    id = "webdav",
    base = file("webdav"),
    settings = buildProperties
  ) dependsOn Web.module

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-webdav",
    libraryDependencies ++= deps
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

  val deps = Seq(servletApiProvided, miltonApi, miltonServlet) ++ miltonApiDeps
}

object WebEditor extends Build {

  lazy val module = Project(
    id = "webeditor",
    base = file("webeditor"),
    settings = buildProperties
  ) dependsOn (Publet.module, Web.module, Ext.module)

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-webeditor",
    libraryDependencies ++= deps
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

  val deps = Seq(servletApiProvided, grizzledSlf4j, scalaTest)

}

object Ext extends Build {

  lazy val module = Project(
    id = "ext",
    base = file("ext"),
    settings = buildProperties
  ) dependsOn (Publet.module, Web.module, GitrWeb.module, Webdav.module)

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-ext",
    libraryDependencies ++= deps
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

  val deps = Seq(squareMail, servletApiProvided, grizzledSlf4j, scalaTest, blueprintsCore, colt, orientdb, blueprintsOrient, scue)

}

object Doc extends Build {

  lazy val module = Project(
    id = "doc",
    base = file("doc"),
    settings = buildProperties
  ) dependsOn Web.module

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-doc",
    // add the WebExtension source file to have it available from the docs
    resourceGenerators in Compile <+= (sourceDirectory in Web.module, resourceManaged in Compile) map { (sd:File, rd: File) =>
      val sourceDir = sd / "main" / "scala" / "org" / "eknet" / "publet" / "web"
      val sources = Seq(sourceDir / "WebExtension.scala", sourceDir / "req" / "RequestHandlerFactory.scala")
      val target = rd / "org" / "eknet" / "publet" / "doc" / "resources" / "_sources"
      sources.map( f => {
        val targetFile = target / f.name
        IO.copyFile(f, targetFile)
        targetFile
      })
    },
    libraryDependencies ++= deps
  )

  val deps = Seq(grizzledSlf4j, servletApiProvided)
}

// modules to create deployable units
object War extends Build {

  import com.github.siasia._
  import WebappPlugin.webappSettings

  lazy val module = Project(
    id = "war",
    base = file("war"),
    settings = buildProperties
  ) dependsOn (Publet.module, ScalateEngine.module, Web.module,
    WebEditor.module, Ext.module, GitrWeb.module, Doc.module, Webdav.module)

  val buildProperties = Project.defaultSettings ++ webappSettings ++ Seq[Project.Setting[_]](
    name := "publet-war",
    publishArtifact in (Compile, packageBin) := true,
  //todo find out how to better remove any test artifact
    PluginKeys.warPostProcess in Compile <<= (target) map {
      (target) => {
        () =>
          val webapp = target / "webapp"
          IO.delete(webapp / "WEB-INF" / "lib" / "scue_2.9.2-test.jar")
          IO.delete(webapp / "WEB-INF" / "lib" / "scalatest_2.9.2-2.0.M6-SNAP3.jar")
      }
    },
    libraryDependencies ++= deps
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

  val deps = Seq(slf4jApi, slf4jJcl, servletApiProvided, logbackClassic)
}


object Server extends Build {
  import sbtassembly.Plugin._
  import AssemblyKeys._
  import com.github.siasia._

  val serverDist = TaskKey[File]("server-dist", "Creates a distributable zip file containing the publet standalone server.")

  lazy val module = Project(
    id = "server",
    base = file("server"),
    settings = buildProperties
  )

  val buildProperties = Project.defaultSettings ++ assemblySettings ++ Seq[Project.Setting[_]](
    name := "publet-server",
    mergeStrategy in assembly <<= (mergeStrategy in assembly) { (old) =>
      {
        case "about.html" => MergeStrategy.concat
        case x => old(x)
      }
    },
    serverDist <<= (AssemblyKeys.assembly, PluginKeys.packageWar.in(War.module).in(Compile), Keys.target, Keys.name, Keys.version, Keys.sourceDirectory) map { (server:File, war:File, target:File, name:String, version:String, sourceDir: File) =>
      val distdir = target / (name +"-"+ version)
      val zipFile = target / (name +"-"+ version +".zip")
      IO.delete(zipFile)
      IO.delete(distdir)

      val etc = distdir / "etc"
      val bin = distdir / "bin"
      val webapp = distdir / "webapp"
      IO.createDirectories(Seq(distdir, etc, bin, webapp))

      IO.unzip(war, webapp)
      //remove logging dependencies, it's included in publet-server.jar
      IO.listFiles(webapp/ "WEB-INF" / "lib", FileFilter.globFilter("logback*")).map(IO.delete)
      IO.listFiles(webapp/ "WEB-INF" / "lib", FileFilter.globFilter("slf4j-api*")).map(IO.delete)
      IO.listFiles(webapp/ "WEB-INF" / "lib", FileFilter.globFilter("grizzled-slf4*")).map(IO.delete)
      //remove scala library
      IO.listFiles(webapp/ "WEB-INF" / "lib", FileFilter.globFilter("scala-library*")).map(IO.delete)

      //copy some resources
      val distResources = sourceDir / "dist"
      IO.copyFile(target.getParentFile.getParentFile / "LICENSE.txt", distdir / "LICENSE.txt")
      IO.copyFile(target.getParentFile.getParentFile / "NOTICE.md", distdir / "NOTICE.md")
      IO.copyFile(target.getParentFile.getParentFile / "README.md", distdir / "README.md")
      IO.listFiles(distResources / "bin").map(f => IO.copyFile(f, bin / f.getName))
      IO.listFiles(distResources / "etc").map(f => IO.copyFile(f, etc / f.getName))

      val serverFile = bin / "publet-server.jar"
      IO.copyFile(server, serverFile)

      def entries(f: File):List[File] = f :: (if (f.isDirectory) IO.listFiles(f).toList.flatMap(entries(_)) else Nil)
      IO.zip(entries(distdir).map(d => (d, d.getAbsolutePath.substring(distdir.getParent.length +1))), zipFile)
      zipFile
    },
    libraryDependencies ++= deps
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

  val deps = Seq(slf4jApi, grizzledSlf4j, jettyServer, jettyAjp, logbackClassic, bouncyCastleProv, bouncyCastleMail)
}

object App extends Build {

  lazy val module = Project(
    id = "app",
    base = file("app"),
    settings = buildProperties
  ) dependsOn(War.module, Server.module)

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-app"
  ) ++ net.virtualvoid.sbt.graph.Plugin.graphSettings

}
