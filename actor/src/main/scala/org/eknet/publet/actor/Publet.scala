package org.eknet.publet.actor

import akka.actor._
import com.typesafe.config.{Config, ConfigFactory}
import scala.reflect.ClassTag
import java.nio.file.{Path => JPath, Files, Paths}
import org.eknet.publet.content._
import akka.event.LoggingAdapter

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.05.13 11:44
 */
object Publet extends ExtensionId[PubletExt] with ExtensionIdProvider {

  def lookup() = Publet

  def createExtension(system: ExtendedActorSystem) = {
    new PubletExt(system)
  }

}

class PubletExt(system: ExtendedActorSystem) extends Extension {
  val log = akka.event.Logging(system.eventStream, getClass)

  val settings = new PubletSettings(system)
  val classLoader = system.dynamicAccess.classLoader

  /**
   * Simple registry for functions that return partitions for a given URI.
   *
   * The functions are used by the partion-factory actor. It is possible to
   * create actors for this and send the actor-refs to the publet-actor. The
   * `GetPartition` messages are then forwarded to them.
   *
   */
  val partitionFactory = new PartitionFactory
  partitionFactory.register("classpath", new ClasspathPartitonFactory(classLoader))
  partitionFactory.register("var", new SubdirPartitionFactory(settings.workdir))
  partitionFactory.register("tmp", new SubdirPartitionFactory(settings.tempdir))
  partitionFactory.register("file", AbsoluteFsPartitionFactory)

  /**
   * The document root which is used as fallback.
   *
   * The document-root-actor uses this instance, if no mounted
   * actor can be found for a given path. It is therefore
   * possible to send actor refs to the document-root-actor, which
   * will then forward requests to them.
   */
  val documentRoot = new ContentTree()

  /**
   * The engine registry used as fallback, if no engine actor
   * was found. As with `documentRoot` and `partitionFactory`,
   * it is possible to send actor refs to the engine-registry
   * instead of registering them here.
   */
  val engineRegistry = new EngineRegistry()

}

class PubletSettings(system: ExtendedActorSystem) {
  val log = akka.event.Logging(system.eventStream, getClass)

  final val config = {
    val cfg = system.settings.config
    cfg.checkValid(ConfigFactory.defaultReference(), "publet")
    cfg
  }

  import config._
  import collection.JavaConverters._

  if (getBoolean("publet.log-config")) {
    log.info("Merged configuration\n"+ config.getConfig("publet").root().render())
  }

  val workdir = createdir(Paths.get(getString("publet.workdir")))
  val tempdir = createdir(Paths.get(getString("publet.tempdir")))

  val mounts = {
    val cfgs = getConfigList("publet.mounts").asScala
    val entries = for (cfg <- cfgs) yield {
      uri(cfg.getString("uri")) -> cfg.getStringList("paths").asScala.map(p => Path(p)).toSet
    }
    entries.toMap
  }

  val plugins =  {
    val white = getStringList("publet.plugins").asScala.toSet
    val black = getStringList("publet.disabled-plugins").asScala.toSet
    white.diff(black).map(cn => createInstanceOf[Plugin](cn).get).map(c => c.name -> c).toMap
  }

  val stopOnInitError = getBoolean("publet.stop-on-init-error")

  val nrOfInstances = getInt("publet.publet-routing.nr-of-instances")

  private def createdir(path: JPath) = {
    Files.createDirectories(path)
    path
  }

  def createInstanceOf[T: ClassTag](fqcn: String) = {
    lazy val loadObject = system.dynamicAccess.getObjectFor(fqcn)
    lazy val defctor = system.dynamicAccess.createInstanceFor(fqcn, Seq.empty)
    lazy val settingsCtor = system.dynamicAccess.createInstanceFor(fqcn, Seq(classOf[PubletSettings] -> this))
    lazy val configCtor = system.dynamicAccess.createInstanceFor(fqcn, Seq(classOf[Config] -> config))
    loadObject.orElse(settingsCtor).orElse(configCtor).orElse(defctor)
  }

}
