import sbt._
import Keys._
import Dependencies._

object Resolvers {
  val eknet = "eknet.org" at "https://eknet.org/maven2"
  val ettrema = "ettrema.com" at "http://www.ettrema.com/maven2"
}
object Version {
  val slf4j = "1.6.5"
  val logback = "1.0.6"
  val servlet = "3.0.1"
  val ccollections = "3.2.1"
  val cfileupload = "1.2.2"
  val cio = "2.2"
  val squaremail = "1.0.1"
  val jgit = "2.0.0.201206130900-r"
  val shiro = "1.2.1"
  val scalaTest = "2.0.M2"
  val grizzled = "0.6.9"
  val scalate = "1.5.3"
  val mimeUtil = "2.1.3"
  val orientdb = "1.0.1"
  val blueprints = "2.0.0"
  val milton = "1.8.1.3"
  val ccodec = "1.4"
  val jdom = "1.1"
  val jetty = "8.1.7.v20120910"
  val bouncyCastle = "1.46"
  val scala = "2.9.2"
  val yuicompressor = "2.4.7"
  val googleClosureCompiler = "rr2079.1"
  val guava = "12.0"
}

object Dependencies {

  val commonsFileUpload = "commons-fileupload" % "commons-fileupload" % Version.cfileupload exclude("rhino", "js")
  val commonsIo = "commons-io" % "commons-io" % Version.cio withSources() exclude("rhino", "js")
  val blueprints = "com.tinkerpop.blueprints" % "blueprints-orient-graph" % Version.blueprints withSources() intransitive() //uses orientdb 1.0.1
  val blueprintsCore = "com.tinkerpop.blueprints" % "blueprints-core" % Version.blueprints withSources() intransitive()
  val bouncyCastleProv = "org.bouncycastle" % "bcprov-jdk16" % Version.bouncyCastle exclude("rhino", "js")
  val bouncyCastleMail = "org.bouncycastle" % "bcmail-jdk16" % Version.bouncyCastle exclude("rhino", "js")
  val googleClosureCompiler = "com.google.javascript" % "closure-compiler" % Version.googleClosureCompiler intransitive()
  val grizzledSlf4j = "org.clapper" %% "grizzled-slf4j" % Version.grizzled withSources() exclude("rhino", "js") //scala 2.9.2 only
  val guava = "com.google.guava" % "guava" % Version.guava
  val jettyAjp = "org.eclipse.jetty" % "jetty-ajp" % Version.jetty exclude("rhino", "js")
  val jettyContainer = "org.eclipse.jetty" % "jetty-webapp" % "8.0.1.v20110908" % "container" withSources() exclude("rhino", "js")
  val jettyServer = "org.eclipse.jetty" % "jetty-webapp" % Version.jetty exclude("rhino", "js")
  val jgit = "org.eclipse.jgit" % "org.eclipse.jgit" % Version.jgit withSources() exclude("rhino", "js")
  val jgitHttpServer = "org.eclipse.jgit" % "org.eclipse.jgit.http.server" % Version.jgit withSources() exclude("rhino", "js")
  val logbackClassic = "ch.qos.logback" % "logback-classic" % Version.logback withSources() exclude("rhino", "js")
  val miltonApi = "com.ettrema" % "milton-api" % Version.milton withSources() intransitive()
  val miltonApiDeps = Seq(
    "commons-codec" % "commons-codec" % Version.ccodec withSources() exclude("rhino", "js"),
    "commons-collections" % "commons-collections" % Version.ccollections withSources() exclude("rhino", "js"),
    "org.jdom" % "jdom" % Version.jdom exclude("rhino", "js")
  )
  val miltonServlet = "com.ettrema" % "milton-servlet" % Version.milton withSources() intransitive()
  val mimeUtil = "eu.medsea.mimeutil" % "mime-util" % Version.mimeUtil intransitive()
  val orientdbCore = "com.orientechnologies" % "orientdb-core" % Version.orientdb withSources() exclude("rhino", "js")
  val orientCommons = "com.orientechnologies" % "orient-commons" % Version.orientdb withSources() exclude("rhino", "js")
  val scalaCompiler = "org.scala-lang" % "scala-compiler" % Version.scala withSources() exclude("rhino", "js")
  val scalateCore = "org.fusesource.scalate" % "scalate-core" % Version.scalate exclude("rhino", "js")
  val scalateUtil = "org.fusesource.scalate" % "scalate-util" % Version.scalate exclude("rhino", "js")
  val scalatePage = "org.fusesource.scalate" % "scalate-page" % Version.scalate exclude("rhino", "js")
  val scalateWikitext = "org.fusesource.scalate" % "scalate-wikitext" % Version.scalate exclude("rhino", "js")
  val scalaTest = "org.scalatest" %% "scalatest" % Version.scalaTest % "test" withSources() exclude("rhino", "js")
  val servletApi = "javax.servlet" % "javax.servlet-api" % Version.servlet withSources() exclude("rhino", "js")
  val servletApiProvided = servletApi % "provided" exclude("rhino", "js")
  val shiro = "org.apache.shiro" % "shiro-core" % Version.shiro withSources() exclude("rhino", "js")
  val shiroWeb = "org.apache.shiro" % "shiro-web" % Version.shiro withSources() exclude("rhino", "js")
  val slf4jApi = "org.slf4j" % "slf4j-api" % Version.slf4j exclude("rhino", "js")
  val squareMail = "org.eknet.squaremail" % "squaremail" % Version.squaremail exclude("rhino", "js")
  val yuicompressor = "com.yahoo.platform.yui" % "yuicompressor" % Version.yuicompressor
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
      ScalaScriptEngine.module,
      Web.module,
      Gitr.module,
      GitPart.module,
      GitrWeb.module,
      Auth.module,
      ScalateEngine.module,
      War.module,
      WebEditor.module,
      Ext.module,
      Server.module,
      Doc.module,
      App.module
    )

  val buildSettings = Project.defaultSettings ++ Seq(
    name := "publet-parent",
    libraryDependencies ++= deps
  ) ++ container.deploy("/app" -> War.module, "/" -> War.module) ++ Seq(PluginKeys.port in container.Configuration := 8081)

  override lazy val settings = super.settings ++ Seq(
    version := "1.0.0-SNAPSHOT",
    organization := "org.eknet.publet",
    scalaVersion := Version.scala,
    publishTo := Some("eknet-maven2" at "https://eknet.org/maven2"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    exportJars := true,
    scalacOptions ++= Seq("-unchecked", "-deprecation"),
    resolvers := Seq(Resolvers.eknet, Resolvers.ettrema),
    pomExtra := <licenses>
      <license>
        <name>Apache 2</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
      </license>
    </licenses>,

    // see https://jira.codehaus.org/browse/JETTY-1493
    ivyXML := <dependency org="org.eclipse.jetty.orbit" name="javax.servlet" rev="3.0.0.v201112011016">
        <artifact name="javax.servlet" type="orbit" ext="jar"/>
    </dependency>
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
  ) 

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
  ) 

  lazy val deps = Seq(grizzledSlf4j, servletApiProvided, scalaTest)

}


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
  ) 
  
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
  )  

  val deps = Seq(slf4jApi, grizzledSlf4j, scalaTest)

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
  ) 

  val deps = Seq(slf4jApi, scalaCompiler, scalaTest, grizzledSlf4j)

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
  ) 

  val deps = Seq(servletApiProvided,
       slf4jApi, grizzledSlf4j,
       commonsFileUpload,
       commonsIo, 
       jgitHttpServer,
       shiroWeb,
       miltonApi, miltonServlet,
       yuicompressor,
       googleClosureCompiler,
       guava,
       scalaTest) ++ miltonApiDeps

}

object War extends Build {

  import com.github.siasia._
  import WebappPlugin.webappSettings

  lazy val module = Project(
    id = "war",
    base = file("war"),
    settings = buildProperties
  ) dependsOn (Publet.module, GitPart.module, ScalateEngine.module, Web.module,
    WebEditor.module, Ext.module, GitrWeb.module, Doc.module)

  val buildProperties = Project.defaultSettings ++ webappSettings ++ Seq[Project.Setting[_]](
    name := "publet-war",
    publishArtifact in (Compile, packageBin) := true,
    libraryDependencies ++= deps
  )

  val deps = Seq(servletApiProvided, logbackClassic)
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
  )

  val deps = Seq(slf4jApi, grizzledSlf4j, shiro, scalaTest)
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
  )

  val deps = Seq(servletApiProvided, grizzledSlf4j, scalaTest)

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
  ) 

  val deps = Seq(squareMail, servletApiProvided, grizzledSlf4j, scalaTest, blueprints, blueprintsCore, orientdbCore, orientCommons)

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

object Doc extends Build {

  lazy val module = Project(
    id = "doc",
    base = file("doc"),
    settings = buildProperties
  ) dependsOn Web.module

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-doc",
    libraryDependencies ++= deps
  )

  val deps = Seq(grizzledSlf4j, servletApiProvided)
}

object Server extends Build {
  import sbtassembly.Plugin._
  import com.github.siasia._

  val serverDist = TaskKey[File]("server-dist", "Creates a distributable zip file containing the publet standalone server.")

  lazy val module = Project(
    id = "server",
    base = file("server"),
    settings = buildProperties
  )

  val buildProperties = Project.defaultSettings ++ assemblySettings ++ Seq[Project.Setting[_]](
    name := "publet-server",
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
      IO.listFiles(webapp/ "WEB-INF" / "lib", FileFilter.globFilter("slf4j*")).map(IO.delete)
      IO.listFiles(webapp/ "WEB-INF" / "lib", FileFilter.globFilter("grizzled-slf4*")).map(IO.delete)
      //remove scala library
      IO.listFiles(webapp/ "WEB-INF" / "lib", FileFilter.globFilter("scala-library*")).map(IO.delete)

      //copy some resources
      val distResources = sourceDir / "dist"
      IO.listFiles(distResources / "bin").map(f => IO.copyFile(f, bin / f.getName))
      IO.listFiles(distResources / "etc").map(f => IO.copyFile(f, etc / f.getName))

      val serverFile = bin / "publet-server.jar"
      IO.copyFile(server, serverFile)

      def entries(f: File):List[File] = f :: (if (f.isDirectory) IO.listFiles(f).toList.flatMap(entries(_)) else Nil)
      IO.zip(entries(distdir).map(d => (d, d.getAbsolutePath.substring(distdir.getParent.length +1))), zipFile)
      zipFile
    },
    libraryDependencies ++= deps
  )

  val deps = Seq(grizzledSlf4j, servletApi, jettyServer, jettyAjp, logbackClassic, bouncyCastleProv, bouncyCastleMail)
}

object App extends Build {

  lazy val module = Project(
    id = "app",
    base = file("app"),
    settings = buildProperties
  ) dependsOn(War.module, Server.module)

  val buildProperties = Project.defaultSettings ++ Seq[Project.Setting[_]](
    name := "publet-app"
  )
}
