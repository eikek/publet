package org.eknet.publet.graphdb

import akka.actor.{ExtendedActorSystem, Extension, ExtensionId, ExtensionIdProvider}
import org.eknet.publet.actor.Publet
import java.nio.file.{Files, Path}
import com.tinkerpop.blueprints.Graph
import com.tinkerpop.blueprints.impls.orient.OrientGraph
import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import com.orientechnologies.orient.core.config.OGlobalConfiguration
import com.orientechnologies.orient.core.db.graph.OGraphDatabase

object GraphdbExt extends ExtensionIdProvider with ExtensionId[GraphdbExtImpl] {
  def lookup() = GraphdbExt

  def createExtension(system: ExtendedActorSystem) = {
    val databaseDir = Publet(system).settings.workdir.resolve("databases")
    new GraphdbExtImpl(databaseDir)
  }
}

class GraphdbExtImpl(dbdir: Path) extends Extension {

  Files.createDirectories(dbdir)

  //disable l1 cache. See http://tinkerpop.com/docs/wikidocs/blueprints/2.1.0/OrientDB-Implementation.html
  //OGlobalConfiguration.CACHE_LEVEL1_ENABLED.setValue(false)

  Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
    def run() {
      graphs.get.values.foreach(g => g.db.shutdown())
    }
  }))

  private val graphs = new AtomicReference(Map.empty[String, Supplier])

  def getOrCreate(name: String): BlueprintsGraph = register(name).db

  @tailrec
  private def register(name: String): Supplier = {
    val map = graphs.get
    map.get(name) match {
      case None => {
        val next = map.updated(name, new Supplier(name))
        graphs.compareAndSet(map, next)
        register(name)
      }
      case Some(s) => s
    }
  }

  def newGraph(name: String): BlueprintsGraph = {
    val db = dbdir.resolve(name)
    val og = new OrientGraph("local://"+ db.toAbsolutePath.toString) with BlueprintsGraph
    og.getRawGraph.setLockMode(OGraphDatabase.LOCK_MODE.RECORD_LEVEL_LOCKING)
    og
  }

  private class Supplier(name: String) {
    lazy val db = newGraph(name)
  }
}


