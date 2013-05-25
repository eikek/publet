package org.eknet.publet.scalate

import akka.actor.{ActorSystem, ActorRef, Props}
import org.eknet.publet.actor.{PubletExt, Publet, Plugin}
import akka.util.Timeout
import scala.concurrent.{ExecutionContext, Await, Future}
import org.eknet.publet.content.{Content, Resource, uri}
import org.eknet.publet.actor.messages._
import org.fusesource.scalate.TemplateEngine
import akka.routing.RoundRobinRouter
import com.typesafe.scalalogging.slf4j.Logging
import org.fusesource.scalate.util.ResourceLoader
import java.util.concurrent.Executors

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.05.13 22:59
 */
object ScalatePlugin extends Plugin with Logging {

  import akka.pattern.ask
  import concurrent.duration._
  implicit val timeout: Timeout = 5.seconds

  private val partitionUri = uri("classpath://org/eknet/publet/scalate/views")

  def name = "scalate"
  def dependsOn = Set()

  def load(ref: ActorRef, system: ActorSystem) = {
    val publet = Publet(system)
    publet.documentRoot.send(dr => dr.mount("/publet/scalate", publet.partitionFactory().create(partitionUri)))
    val settings = PubletScalate(system).scalateSettings

    val scriptRef = system.actorOf(Props[ScalaScriptEngine], "scalascript-engine")
    val scriptF = ref ? Register(scriptRef, settings.scriptCompilerConfig.patterns)

    val engines = createEngineActors(system, ref)
    if (engines.isEmpty) {
      Future.successful("OK")
    } else {
      import system.dispatcher
      val futures = engines map {
        case (name, actor) => {
          val patterns = settings.engineConfigs(name).patterns
          patterns.toSeq match {
            case Seq() => Future.successful("OK")
            case nonempty => {
              logger.info(s"Registering scalate engine $name to patterns ${patterns.map(_.absoluteString).mkString(", ")}")
              ref ? Register(actor, patterns)
            }
          }
        }
      }
      Future.sequence(scriptF :: futures.toList)
    }
  }

  def createEngineActors(system: ActorSystem, ref: ActorRef): Map[String, ActorRef] = {
    val ext = PubletScalate(system)
    val settings = ext.scalateSettings
    createTemplateEnginesFromConfig(ext, Publet(system), ref) map {
      case (name, e) => {
        val nrInst = settings.engineConfigs(name).nrInstances
        logger.info(s"Creating scalate template engine '$name' with config ${settings.engineConfigs(name)}")
        val props = Props(new ScalateEngine(e, ref)).withRouter(RoundRobinRouter(nrOfInstances = nrInst))
        name -> system.actorOf(props, name = "scalate-"+name)
      }
    }
  }

  def createTemplateEnginesFromConfig(ext: PubletScalateExt, publet: PubletExt, ref: ActorRef): Map[String, ConfiguredEngine] = {
    val settings = ext.scalateSettings
    settings.engineConfigs map {
      case (name, cfg) => name -> {
        val e = new ConfiguredEngine(new TemplateEngine())
        e.engine.resourceLoader = new Loader(ref, publet)
        ext.initialize(name, e)
        e
      }
    }
  }

  private class Loader(ref: ActorRef, publet: PubletExt) extends ResourceLoader with Logging {
    implicit private val executor = ExecutionContext.fromExecutor(Executors.newFixedThreadPool(2))
    def resource(uri: String) = {
      logger.debug(s"Scalate lookup for resource '$uri'.")
      val res = publet.documentRoot().find(uri)
      logger.info("Got result "+ res)
      res match {
        case Some(c: Content) => Some(new ContentTemplate(uri, c))
        case Some(r) => logger.error(s"Cannot process non-content resource '$r' "); None
        case _ => None
      }
    }
  }
}
