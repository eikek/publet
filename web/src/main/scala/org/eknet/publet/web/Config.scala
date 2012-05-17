package org.eknet.publet.web

import java.io.{FileInputStream, File}
import System._
import util.PropertiesMap
import grizzled.slf4j.Logging

/** Configuration file, is picked up from the configured publet directory. This is
 * either given as system property `publet.dir`, as environment variable `PUBLET_DIR`
 * or as servlet config parameter `publet.dir` (which is added to the system properties).
 *
 * This map is read only once on startup. You can reload its contents manually using
 * `#reload()`.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 17.04.12 09:30
 *
 */
object Config extends PropertiesMap with Logging {

  /**
   * The root config directory. Usually retrieved via `publet.dir` system
   * property or `$PUBLET_DIR`  environment variable (in that order)
   *
   */
  private val rootDirectory = {
    var dir = Option(getProperty("publet.dir"))
    if (!dir.isDefined) {
      info("System property 'publet.dir' not defined.")
      dir = Option(getenv().get("PUBLET_DIR"))
    }
    if (!dir.isDefined) {
      info("Environment variable 'PUBLET_DIR' not defined. Falling back to default.")
      dir = Option(getProperty("user.home")+ File.separator +".publet")
    }

    info("Using publet directory: "+ dir.get)
    val d = new File(dir.get)
    if (!d.exists()) if (!d.mkdirs()) throw new RuntimeException("unable to create config dir: "+ d)
    if (!d.isDirectory) throw new RuntimeException("Config dir is not a directory: "+d)
    d
  }

  private var directory: File = null
  private[web] def setContextPath(str: String) {
    if (!str.isEmpty)
      this.directory = new File(rootDirectory, str)
    else
      this.directory = new File(rootDirectory, "root")

    info("Loading configuration file: "+ configfile)
    reload()
  }

  /**
   * The directory content root.
   */
  def repositories = subdir("repositories")

  /**
   * publet global configuration file `publet.properties`
   *
   */
  def configfile = new File(directory, "publet.properties")

  /**
   * Returns the configured string to use as prefix for
   * the main content. Defaults to `main`
   *
   * @return
   */
  def mainMount = apply("publet.mainMount").getOrElse("main")

  /**
   * Returns the configured string where the GitServlet
   * is listening.
   *
   * @return
   */
  def gitMount = apply("publet.gitMount").getOrElse("git")

  def getFile(name: String) = new File(directory, name)

  protected def file = if (configfile.exists()) Some(new FileInputStream(configfile)) else None

  private def subdir(name: String) = {
    val d = new File(directory, name)
    if (!d.exists()) if (!d.mkdirs()) throw new RuntimeException("unable to create dir: "+ d)
    if (!d.isDirectory) throw new RuntimeException("Not a directory: "+ d)
    d
  }
}
