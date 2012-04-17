package org.eknet.publet.web

import collection.JavaConversions._
import java.util.Properties
import java.io.{FileInputStream, File}
import org.slf4j.LoggerFactory
import System._

/** Configuration file, is picked up from the configured publet directory. This is
 * either given as system property `publet.dir`, as environment variable `PUBLET_DIR`
 * or as servlet config parameter `publet.dir` (which is added to the system properties).
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 17.04.12 09:30
 *
 */
object Config {
  private val log = LoggerFactory.getLogger(getClass)

  val directory = new File(Option(getProperty("publet.dir"))
    .getOrElse(Option(getenv().get("PUBLET_DIR"))
    .getOrElse(getProperty("user.home")+ File.separator +".publet")))

  if (!directory.exists()) if (!directory.mkdirs()) throw new RuntimeException("unable to create config dir: "+ directory)
  if (!directory.isDirectory) throw new RuntimeException("Config dir is not a directory: "+directory)

  log.info("Setup publet dir to: "+ directory)

  val contentRoot = subdir("contents")

  private val file = new File(directory, "publet.properties")
  private val props = new Properties(); reload();

  def value(key: String) = Option(props.getProperty(key))

  def keySet = props.stringPropertyNames().toSet

  def reload() {
    synchronized {
      props.clear()
      if (file.exists()) props.load(new FileInputStream(file))
    }
  }

  private def subdir(name: String) = {
    val d = new File(directory, name)
    if (!d.exists()) if (!d.mkdirs()) throw new RuntimeException("unable to create dir: "+ d)
    if (!d.isDirectory) throw new RuntimeException("Not a directory: "+ d)
    d
  }
}
