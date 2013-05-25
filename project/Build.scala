import sbt._
import sbt.Keys._

object Resolvers {
  val eknet = "eknet.org" at "https://eknet.org/maven2"
  val spray = "spray.io" at "http://repo.spray.io"
}

object Version {
  
  val scala = "2.10.1"
  val slf4j = "1.7.4"
  val scalaLogging = "1.0.1"
  val akka = "2.1.4"
  val spray = "1.1-M7"
  val sprayJson = "1.2.3"
  val logback = "1.0.12"
  val scalaTest = "1.9.1"
  val config = "1.0.0"
  val jgit = "2.3.1.201302201838-r"

}

object Deps {

  val loggingApi = Seq(
    "org.slf4j" % "slf4j-api" % Version.slf4j,
    "com.typesafe" %% "scalalogging-slf4j" % Version.scalaLogging
  )

  val config = Seq(
    "com.typesafe" % "config" % Version.config
  )

  val akka = Seq(
    "com.typesafe.akka" %% "akka-slf4j" % Version.akka,
    "com.typesafe.akka" %% "akka-actor" % Version.akka,
    "com.typesafe.akka" %% "akka-agent" % Version.akka,
    "com.typesafe.akka" %% "akka-testkit" % Version.akka % "test"
  )

  val spray = Seq(
    "io.spray" % "spray-routing" % Version.spray,
    "io.spray" %% "spray-json" % Version.sprayJson
  )

  val scalaCompiler = Seq(
    "org.scala-lang" % "scala-compiler" % Version.scala
  )

  val test = Seq(
    "org.scalatest" %% "scalatest" % Version.scalaTest
  ) map (_ % "test")

  val logback = Seq(
    "ch.qos.logback" % "logback-classic" % Version.logback % "runtime"
  )
}

object Build extends sbt.Build {

  lazy val root = Project(
    id = "parent",
    base = file("."),
    settings = Project.defaultSettings ++ Seq(
      name := "publet2",
      publishLocal := {},
      publish := {},
      unmanagedSourceDirectories in Compile := Seq(),
      unmanagedSourceDirectories in Test := Seq(),
      unmanagedResourceDirectories := Seq()
    )
  ) aggregate (PubletLib.module, PubletActor.module, WebApp.module, Dist.module, ScalatePlugin.module, GitPlugin.module, GraphdbPlugin.module)
  
  override lazy val settings = super.settings ++ Seq(
    version := "2.0.0-SNAPSHOT",
    organization := "org.eknet.publet",
    scalaVersion := Version.scala,
    publishTo := Some("eknet-maven2" at "https://eknet.org/maven2"),
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-language:implicitConversions"),
    resolvers ++= Seq(Resolvers.eknet, Resolvers.spray),
    libraryDependencies ++= Deps.test,
    licenses := Seq(("ASL2", url("http://www.apache.org/licenses/LICENSE-2.0.txt")))
  )
}

object PubletLib extends sbt.Build {

  lazy val module = Project(
    id = "content",
    base = file("content"),
    settings = Project.defaultSettings ++ Seq(
      name := "publet-content",
      description := "This is the library providing the low-level building blocks. It has the least possible dependencies."
    )
  )
}

object PubletActor extends sbt.Build {
  import sbtbuildinfo.Plugin._

  lazy val module = Project(
    id = "actor",
    base = file("actor"),
    settings = Project.defaultSettings ++
      buildInfoSettings ++
      Seq(
        name := "publet-actor",
        sourceGenerators in Compile <+= buildInfo,
        buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, BuildInfoKey.action("buildTime") {
          System.currentTimeMillis
        }),
        buildInfoPackage := "org.eknet.publet.actor",
        libraryDependencies ++= Deps.akka.tail
      )
  ) dependsOn (PubletLib.module)

}

object WebApp extends sbt.Build {

  val deps = Seq(
    "org.eclipse.jgit" % "org.eclipse.jgit.http.server" % Version.jgit,
    "javax.servlet" % "servlet-api" % "2.5"
  )

  lazy val module = Project(
    id = "webapp",
    base = file("webapp"),
    settings = Project.defaultSettings ++ Seq(
      name := "publet-webapp",
      libraryDependencies ++= Deps.akka ++ Deps.spray ++ deps
    )
  ) dependsOn (PubletActor.module, ScalatePlugin.module, GitPlugin.module, GraphdbPlugin.module)
}

object Dist extends sbt.Build {
  import akka.sbt.AkkaKernelPlugin._

  val deps = Seq(
    "io.spray" % "spray-can" % Version.spray,
    "com.typesafe.akka" %% "akka-kernel" % Version.akka
  )

  lazy val module = Project(
      id = "dist",
      base = file("dist"),
      settings = Project.defaultSettings ++ distSettings ++ Seq(
        name := "publet-dist",
        distBootClass := "org.eknet.publet.dist.Boot",
        libraryDependencies ++= Deps.logback ++ deps
      )
  ) dependsOn (WebApp.module)
}


object ScalatePlugin extends sbt.Build {

  val scalateVersion = "1.6.1"
  val scalateDeps = Seq(
   "org.fusesource.scalate" %% "scalate-core" % scalateVersion,
   "org.fusesource.scalate" %% "scalate-util" % scalateVersion,
   "org.fusesource.scalate" %% "scalate-page" % scalateVersion,
   "org.fusesource.scalate" %% "scalate-wikitext" % scalateVersion,
   "org.fusesource.scalamd" %% "scalamd" % "1.6"
  ) map( _ exclude("org.slf4j", "slf4j-api") exclude("rhino", "js") intransitive())
  val compressors = Seq(
    "com.yahoo.platform.yui" % "yuicompressor" % "2.4.7" exclude("rhino", "js"),
    "rhino" % "js" % "1.7R2"
  )

  lazy val module = Project(
    id = "scalate",
    base = file("plugins") / "scalate",
    settings = Project.defaultSettings ++ Seq(
      name := "publet-scalate",
      libraryDependencies ++= scalateDeps ++ Deps.loggingApi ++ Deps.scalaCompiler ++ compressors
    )
  ) dependsOn (PubletActor.module)
}

object GitPlugin extends sbt.Build {

  val pluginDeps = Seq(
    "org.eclipse.jgit" % "org.eclipse.jgit" % Version.jgit
  )

  lazy val module = Project(
    id = "gitr",
    base = file("plugins") / "gitr",
    settings = Project.defaultSettings ++ Seq(
      name := "publet-gitr",
      libraryDependencies ++= pluginDeps ++ Deps.loggingApi
    )
  ) dependsOn (PubletActor.module)
}

object GraphdbPlugin extends sbt.Build {

  val moduleDeps = Seq(
    "com.tinkerpop.blueprints" % "blueprints-core" % "2.3.0",
    "com.tinkerpop.blueprints" % "blueprints-orient-graph" % "2.3.0"
  )

  val module = Project (
    id = "graphdb",
    base = file("plugins") / "graphdb",
    settings = Project.defaultSettings ++ Seq(
      name := "publet-graphdb",
      libraryDependencies ++= Deps.loggingApi ++ moduleDeps
    )
  ) dependsOn (PubletActor.module)
}
