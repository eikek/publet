package org.eknet.publet.ext.counter

import com.tinkerpop.blueprints.impls.orient.OrientGraph
import org.eknet.publet.web.Config
import java.io.File
import com.tinkerpop.blueprints.TransactionalGraph.Conclusion
import com.tinkerpop.blueprints.Vertex
import org.fusesource.scalate.util.Logging

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.06.12 23:46
 */
private[counter] class CounterDb extends Logging {

  /** The label of the edge from the reference node to each uri node */
  val pageEdgeLabel = "page"

  /** The property key of the uri value */
  val pagePathKey = "page_pagePath"

  /** The property key of the count value */
  val pageCountKey = "page_accessCount"

  /** The property key of the last access time value */
  val pageLastAccessKey = "page_lastAccess"


  val dbUri = "local://"+new File(Config.configDirectory, "databases"+ File.separator +"extdb").getAbsolutePath
  val graph = new OrientGraph(dbUri)
  //creates auto-index on the pagePathKey property
  graph.createKeyIndex(pagePathKey, classOf[Vertex])

  private val txthread = new ThreadLocal[Boolean]() {
    override def initialValue() = false
  }

  /**
   * Wraps the function in a transaction. Supports
   * nested blocks, but only starts a transaction if
   * none exists on the current thread.
   *
   * @param f
   * @tparam A
   * @return
   */
  def withTx[A](f: OrientGraph=>A) = {
    if (!txthread.get()) {
      graph.startTransaction()
      txthread.set(true)
      try {
        val r = f(graph)
        graph.stopTransaction(Conclusion.SUCCESS)
        r
      } catch {
        case e: Throwable => {
          graph.stopTransaction(Conclusion.FAILURE)
          throw e
        }
      } finally {
        txthread.remove()
      }
    } else {
      f(graph)
    }
  }

  /**
   * The reference node that is created if not there. This is a
   * entry point node that acts as a super-node where each uri is
   * connected to.
   * It is right now not used for querying, but it serves to organize
   * page-uri nodes.
   *
   * Besides that it holds one property `version` with the current
   * graph scheme (which currently is this single node).
   */
  lazy val referenceNode = {
    import collection.JavaConversions._
    val currentVersion = 0
    val referenceProperty = "__9b042b5a-bb0d-4656-a037-2b5b78fd543f"
    withTx { db =>
      db.getVertices(referenceProperty, 0).headOption match {
        case Some(v) => v
        case None => {
          db.createKeyIndex(referenceProperty, classOf[Vertex])
          val v = db.addVertex()
          v.setProperty(referenceProperty, 0)
          v.setProperty(versionProperty, currentVersion)
          v
        }
      }
    }
  }

  private val versionProperty = "__counterDbVersion"

  def shutdown() {
    info("Shutting down graph database: "+ graph)
    graph.shutdown()
  }
}
