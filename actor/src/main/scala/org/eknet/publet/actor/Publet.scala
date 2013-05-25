package org.eknet.publet.actor

import akka.actor._
import com.typesafe.config.{Config, ConfigFactory}
import scala.reflect.ClassTag
import java.nio.file.{Path => JPath, Files, Paths}
import org.eknet.publet.content._
import scala.collection.Map
import scala.util.Success
import org.eknet.publet.actor.messages.{ResourceDeleted, ContentCreated, FolderCreated}
import akka.agent.Agent
import org.eknet.publet.actor.docroot.DocumentRoot

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 05.05.13 11:44
 */
object Publet extends ExtensionId[PubletExt] with ExtensionIdProvider {

  def lookup() = Publet

  def createExtension(system: ExtendedActorSystem) = {
    new PubletExt(system)
  }

  // either the remaining tree or the list of layers
  type SortedPlugins = Either[Map[String, Set[String]], List[List[String]]]

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
  val partitionFactory = Agent(createParitionSuppliers)(system)

  def createPartition(uri: String) = partitionFactory().create(uri)

  private def createParitionSuppliers = {
    val map = new PartitionSupplierMap()
    map.update("classpath", new ClasspathPartitonFactory(classLoader))
      .update("var", new SubdirPartitionFactory(settings.workdir))
      .update("tmp", new SubdirPartitionFactory(settings.tempdir))
      .update("file", AbsoluteFsPartitionFactory)
  }

  /**
   * The document root.
   */
  val documentRoot = Agent(new DocumentRoot(EmptyPartition, Map.empty, system.eventStream))(system)


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

  val plugins = {
    val white = getStringList("publet.plugins").asScala.toSet
    val black = getStringList("publet.disabled-plugins").asScala.toSet
    white.diff(black).map(cn => createInstanceOf[Plugin](cn).get)
  }.map(c => c.name -> c).toMap

//  val plugins = pluginObjects.collect({ case p: Plugin => p }).map(c => c.name -> c).toMap

  val pluginsSorted = sortLayers(plugins.values)

  val stopOnInitError = getBoolean("publet.stop-on-init-error")

  private def createdir(path: JPath) = {
    Files.createDirectories(path)
    path
  }

  def createInstanceOf[T: ClassTag](fqcn: String) = {
    lazy val loadObject = system.dynamicAccess.getObjectFor(fqcn)
    lazy val defctor = system.dynamicAccess.createInstanceFor(fqcn, Seq.empty)
    lazy val configCtor = system.dynamicAccess.createInstanceFor(fqcn, Seq(classOf[Config] -> config))
    lazy val systemCtor = system.dynamicAccess.createInstanceFor(fqcn, Seq(classOf[ActorSystem] -> system))
    loadObject.orElse(systemCtor).orElse(configCtor).orElse(defctor)
  }

  private def sortLayers(list: Iterable[Plugin]): Publet.SortedPlugins = {
    val tree = list.map(el => (el.name -> el.dependsOn)).toMap
    utils.toposortLayers(tree)
  }
}
