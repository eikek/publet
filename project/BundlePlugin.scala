import sbt._
import Keys._
import java.util.jar.Attributes.Name._
import sbt.Defaults._
import sbt.Package.ManifestAttributes
import sbt.Fork.ForkJava


object BundlePlugin extends Plugin {

  val bundle = TaskKey[File]("bundle", "Create a OSGi Bundle artifact in addition to the regular artifact")

  val BundleTool = config("bundle-tool").hide

  // The settings for the plugin. Add `seq(bundleSettings: _*)` to build.sbt
  lazy val bundleSettings = Seq[Project.Setting[_]](
    // Register an ivy configuration to download the BND tool.
    ivyConfigurations += BundleTool,

    // Depend on the BND tool
    libraryDependencies ++= Seq(
      "biz.aQute" % "bnd" % "0.0.384" % "bundle-tool"
    ),

    // A temp directory to use in the up-to-date checking, that
    // avoids running BND unless the main artifact has changed.
    cacheDirectory in bundle <<= cacheDirectory / bundle.key.label,

    // Naming convention for the extra artifact
    artifact in bundle <<= moduleName(Artifact(_, "bnd")),

    // The file where we will create the addition artfiactl
    artifactPath in bundle <<= artifactPathSetting(artifact in bundle),

    // Publish the extra artifact
    publishArtifact in bundle <<= publishMavenStyle,

    // The main task that depends on the main artifact `pacakgeBin in Compile`, and
    // runs the main class `aQute.bnd.main.bnd` in a forked process.
    bundle <<= (packageBin in Compile,
      artifactPath in bundle,
      cacheDirectory in bundle,
      update,
      resolvedScoped,
      javaHome,
      classpathTypes,
      streams) map {
      (jarFile, output, cacheDir, updateReport,
       resolvedScoped, javaHome, classpathTypes, s) =>
        import s.log

        def runBnd: File = {
          val jvmOptions: Seq[String] = {
            // Extract the classpath for the BND tool from the update report
            val toolClasspath: Seq[File] = Classpaths.managedJars(BundleTool, classpathTypes, updateReport).map(_.data)

            val jvmCpOptions = Seq("-classpath", toolClasspath.mkString(java.io.File.pathSeparator))
            val mainClass = "aQute.bnd.main.bnd"
            jvmCpOptions ++ Seq(mainClass, "wrap", output.getAbsolutePath)
          }

          log.info("Running bndlib over %s in %s".format(jarFile.getAbsolutePath, Project.displayFull(resolvedScoped)))
          log.debug("bndlib java command line: " + jvmOptions.mkString("\n"))

          IO.copyFile(jarFile, output)
          val returnCode = (new ForkJava("java")).apply(javaHome, jvmOptions, log)

          if (returnCode != 0)
            sys.error("Non zero return code from bndlib [%d]".format(returnCode))
          else log.info("Created %s".format(output.getAbsolutePath))

          output
        }

        // Only run if inputs have changed.
        val cachedFun = FileFunction.cached(cacheDir, FilesInfo.lastModified, FilesInfo.exists) {
          (in: Set[File]) => Set(runBnd)
        }
        cachedFun(Set(jarFile)).head
    }
  )
}