package org.eknet.publet.web

import java.io.{FileInputStream, File}
import org.slf4j.LoggerFactory
import System._
import util.PropertiesMap

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
object Config extends PropertiesMap {
  private val log = LoggerFactory.getLogger(getClass)

  /**
   * The root config directory. Usually retrieved via `publet.dir` system
   * property or `$PUBLET_DIR`  environment variable (in that order)
   *
   */
  val directory = {
    val d = new File(Option(getProperty("publet.dir"))
      .getOrElse(Option(getenv().get("PUBLET_DIR"))
      .getOrElse(getProperty("user.home")+ File.separator +".publet")))

    if (!d.exists()) if (!d.mkdirs()) throw new RuntimeException("unable to create config dir: "+ d)
    if (!d.isDirectory) throw new RuntimeException("Config dir is not a directory: "+d)
    d
  }

  /**
   * The directory content root.
   */
  val contentRoot = subdir("contents")

  /**
   * publet global configuration file `publet.properties`
   *
   */
  val configfile = new File(directory, "publet.properties")
  log.info("Setup publet dir to: "+ directory)
  reload()

  /**
   * Returns the configured string to use as prefix for
   * the main content. Defaults to `main`
   *
   * @return
   */
  def mainMount = apply("publet.mainMount").getOrElse("main")

  def getFile(name: String) = new File(directory, name)

  protected def file = if (configfile.exists()) Some(new FileInputStream(configfile)) else None

  private def subdir(name: String) = {
    val d = new File(directory, name)
    if (!d.exists()) if (!d.mkdirs()) throw new RuntimeException("unable to create dir: "+ d)
    if (!d.isDirectory) throw new RuntimeException("Not a directory: "+ d)
    d
  }
}
