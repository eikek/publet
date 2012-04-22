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

  val directory = new File(Option(getProperty("publet.dir"))
    .getOrElse(Option(getenv().get("PUBLET_DIR"))
    .getOrElse(getProperty("user.home")+ File.separator +".publet")))

  if (!directory.exists()) if (!directory.mkdirs()) throw new RuntimeException("unable to create config dir: "+ directory)
  if (!directory.isDirectory) throw new RuntimeException("Config dir is not a directory: "+directory)

  log.info("Setup publet dir to: "+ directory)

  val contentRoot = subdir("contents")
  private val configfile = new File(directory, "publet.properties")


  lazy val mainMount = apply("publet.mainMount").getOrElse("main")


  reload()

  def file = if (configfile.exists()) Some(new FileInputStream(configfile)) else None

  private def subdir(name: String) = {
    val d = new File(directory, name)
    if (!d.exists()) if (!d.mkdirs()) throw new RuntimeException("unable to create dir: "+ d)
    if (!d.isDirectory) throw new RuntimeException("Not a directory: "+ d)
    d
  }
}
