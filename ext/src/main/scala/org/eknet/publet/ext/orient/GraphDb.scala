package org.eknet.publet.ext.orient

import com.tinkerpop.blueprints.Vertex
import com.orientechnologies.orient.core.exception.OConcurrentModificationException
import org.fusesource.scalate.util.Logging
import org.eknet.scue.GraphDsl

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.06.12 16:19
 */
class GraphDb(val graph: BlueprintGraph) extends Logging {

  import org.eknet.scue.GraphDsl._

  /**
   * Wraps the function in a transaction.
   *
   * It retries a few times on concurrent modification
   * errors.
   *
   * @param f
   * @tparam A
   * @return
   */
  def withTx[A](f: => A):A = {
    executeOpt(0, 5, db => f)
  }

  /**
   * Wraps the function in a transaction.
   *
   * The function receives the [[org.eknet.publet.ext.orient.BlueprintGraph]]
   * as argument. That can be used with the following
   * syntax
   *
   * <pre>
   *   withTx {
   *     implicit db =>
   *       referenceNode --> "domains" -->| vertex("name", "domainNames")
   *   }
   * </pre>
   *
   * @param f
   * @tparam A
   * @return
   */
  def withTx[A](f: BlueprintGraph => A): A = {
    executeOpt(0, 5, f)
  }

  private def executeOpt[A](count: Int, max: Int, f:BlueprintGraph => A): A = {
    if (count >= max) sys.error("Too many ("+max+") concurrent modifications.")
    try {
      GraphDsl.withTx(f(graph))(graph)
    }
    catch {
      case e: OConcurrentModificationException => {
        error("Concurrent modification error ("+count+"). Trying again...")
        executeOpt(count +1, max, f)
      }
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
    withTx { implicit graph =>
      referenceNode --> label -->| newVertex
    }
  }

  /**
   * The reference node that is created on first access.
   *
   */
  lazy val referenceNode = {
    val referenceProperty = "6b67f6429706419098b4f02923a5a9d5"
    withTx { implicit graph:BlueprintGraph =>
      vertex(referenceProperty, Int.box(0))
    }
  }
}
