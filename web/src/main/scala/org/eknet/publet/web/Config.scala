/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet.web

import java.io.{FileInputStream, File}
import System._
import util.{PubletWeb, PropertiesMap}
import grizzled.slf4j.Logging
import java.nio.file.Files
import com.google.common.eventbus.EventBus
import org.eknet.publet.vfs.Path
import org.eknet.publet.event.Event

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
class Config(contextPath: String, eventBus: EventBus) extends PropertiesMap(eventBus) with Logging {

  private val directory: File = Config.configDirectory(contextPath)


  info("Loading publet.properties file from: "+ configfile.getAbsolutePath)
  reload()


  protected def createEvent() = ConfigReloadedEvent(this)

  /**
   * The configuration directory of this application.
   *
   * @return
   */
  def configDirectory = directory

  /**
   * The root directory containing all repositories
   */
  def repositories = subdir("repositories")

  /**
   * publet global configuration file `publet.properties`
   *
   */
  private lazy val configfile = configFile("publet.properties")

  /**
   * Creates a file beneath the `etc` directory, if in standalone
   * mode or inside the publet directory.
   * @param name
   * @return
   */
  def configFile(name: String) = {
    if (System.getProperty("publet.standalone") != null) {
      new File(new File("etc"), name)
    } else {
      getFile(name)
    }
  }

  private lazy val tempRoot = {
    val dir = if (System.getProperty("publet.standalone") != null) {
      new File(Config.rootDirectory.getParentFile, "temp")
    } else {
      new File(directory, "temp")
    }
    if (!dir.exists()) if (!dir.mkdirs()) sys.error("Cannot create temp directory: "+ dir.getAbsolutePath)
    if (!dir.isDirectory) sys.error("Temp directory is not a directory:" + dir.getAbsolutePath)
    info("Java temp directory is: "+ System.getProperty("java.io.tmpdir"))
    info("Using temporary directory: "+ dir.getAbsolutePath)
    dir
  }

  /**
   * Returns a new directory inside publets config directory
   * with the given name. It is created, if it does not exist
   * yet.
   *
   * This can be used to persist working data.
   *
   * @param name
   */
  def workDir(name: String) = synchronized {
    subdir(name)
  }

  /**
   * Creates a new temporary directory. The name is made of the prefix and
   * some random number/char sequence. The directory is newly created.
   *
   * The directory is not removed automatically on jvm shutdown.
   *
   * @see newStaticTempDir
   * @param prefix
   * @return
   */
  def newTempDir(prefix: String = "publet"): File = Files.createTempDirectory(tempRoot.toPath, prefix).toFile

  /**
   * Creates a directory beneath the temp root using the given name. The directory
   * may already exists, and is created if it does not exist.
   *
   * @param name
   * @return
   */
  def newStaticTempDir(name: String): File = synchronized {
    val t = new File(tempRoot, name)
    if (!t.exists()) t.mkdirs()
    t
  }

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

  /**
   * Returns the current mode the application is in, which is either
   * `development` (the default), or the string defined in `publet.properties`
   * associated to the key `publet.mode`.
   *
   * @return
   */
  def mode: RunMode.Value = RunMode.values
    .find(en => Some(en.toString) == apply("publet.mode"))
    .getOrElse(RunMode.development)

  /**
   * Returns a file beneath configuration directory of this
   * application.
   * @param name
   * @return
   */
  def getFile(name: String) = new File(directory, name)

  protected def file = {
    if (configfile.exists()) {
      Some(new FileInputStream(configfile))
    } else None
  }

  private def subdir(name: String) = {
    val d = new File(directory, name)
    if (!d.exists()) if (!d.mkdirs()) sys.error("unable to create dir: "+ d)
    if (!d.isDirectory) sys.error("Not a directory: "+ d)
    d
  }
}

object Config extends Logging {

  /**
   * The root config directory. Usually retrieved via `publet.dir` system
   * property or `$PUBLET_DIR`  environment variable (in that order)
   *
   */
  val rootDirectory = {
    var dir = Option(getProperty("publet.dir")).collect({ case a:String if (!a.trim.isEmpty) => a})
    if (!dir.isDefined) {
      info("System property 'publet.dir' not defined.")
      dir = Option(getenv().get("PUBLET_DIR")).collect({ case a:String if (!a.trim.isEmpty) => a})
    }
    if (!dir.isDefined) {
      info("Environment variable 'PUBLET_DIR' not defined. Falling back to default.")
      dir = Option(getProperty("user.home")+ File.separator +".publet")
    }

    val d = new File(dir.get)
    info("Using publet directory: "+ d.getAbsolutePath)
    if (!d.exists()) if (!d.mkdirs()) throw new RuntimeException("unable to create config dir: "+ d)
    if (!d.isDirectory) throw new RuntimeException("Config dir is not a directory: "+d)
    d
  }

  def configDirectory(contextPath: String) = contextPath match {
    case "" => new File(Config.rootDirectory, "root")
    case str => new File(Config.rootDirectory, Path(str).segments.mkString("-"))
  }

  /**
   * Returns an instance by looking it up though the injector.
   * @return
   */
  def get = PubletWeb.instance[Config]

  def apply(key: String) = get(key)

}

case class ConfigReloadedEvent(config: Config) extends Event