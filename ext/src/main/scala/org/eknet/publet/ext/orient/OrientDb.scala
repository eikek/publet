package org.eknet.publet.ext.orient

import com.tinkerpop.blueprints.TransactionalGraph.Conclusion
import com.tinkerpop.blueprints.{Edge, Vertex}
import java.io.File
import org.eknet.publet.web.Config

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.06.12 16:19
 */
class OrientDb(val graph: OrientGraph) {

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
  def withTx[A](f: => A):A = {
    if (!txthread.get()) {
      graph.startTransaction()
      txthread.set(true)
      try {
        val r = f
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
      f
    }
  }

  /**
   * Delegates to `graph.shutdonw()`
   */
  def shutdown() {
    graph.shutdown()
  }

  /**
   * Creates a new vertex and adds it to the reference node using
   * the given label to name the edge. The direction is from the
   * reference node to the new node.
   *
   * @param label
   * @return
   */
  def addReferenceVertex(label: String): Vertex = {
    withTx {
      val v = graph.addVertex()
      graph.addEdge(null, referenceNode, v, label)
      v
    }
  }

  /**
   * The reference node that is created on first access.
   *
   */
  lazy val referenceNode = {
    import collection.JavaConversions._
    val referenceProperty = "6b67f6429706419098b4f02923a5a9d5"
    withTx {
      graph.getVertices(referenceProperty, 0).headOption match {
        case Some(v) => v
        case None => {
          graph.createKeyIndex(referenceProperty, classOf[Vertex])
          val v = graph.addVertex()
          v.setProperty(referenceProperty, 0)
          v
        }
      }
    }
  }
}


object OrientDb {

  private val dbroot = {
    val d = new File(Config.configDirectory, "databases")
    new File(d, "orient")
  }

  private def databaseDir(dbname: String) = new File(dbroot, dbname)

  def toOrientUri(dbname: String) = "local://"+ databaseDir(dbname).getAbsolutePath

  /**
   * Creates a new [[com.tinkerpop.blueprints.impls.orient.OrientGraph]] instance.
   *
   * @param name
   * @return
   */
  def newGraph(name: String): OrientGraph = new OrientGraph(toOrientUri(name))

  /**
   * Creates a new [[org.eknet.publet.ext.orient.OrientDb]] with a new
   * instance of a [[com.tinkerpop.blueprints.impls.orient.OrientGraph]].
   *
   * @param name
   * @return
   */
  def newDatabase(name: String): OrientDb = new OrientDb(newGraph(name))

}