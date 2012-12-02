package org.eknet.publet.ext.graphdb

import org.fusesource.scalate.util.Logging
import org.eknet.scue.GraphDsl
import java.io.{InputStream, OutputStream}
import com.tinkerpop.blueprints.util.io.graphml.{GraphMLReader, GraphMLWriter}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.06.12 16:19
 */
class GraphDb(val graph: BlueprintGraph) extends Logging {

  import org.eknet.scue.GraphDsl._

  /**
   * Wraps the function in a transaction.
   *
   * @param f
   * @tparam A
   * @return
   */
  def withTx[A](f: => A):A = {
    GraphDsl.withTx(f)(graph)
  }

  /**
   * Wraps the function in a transaction.
   *
   * The function receives the [[org.eknet.publet.ext.graphdb.BlueprintGraph]]
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
    GraphDsl.withTx(f(graph))(graph)
  }


  /**
   * Delegates to `graph.shutdonw()`
   */
  def shutdown() {
    graph.shutdown()
  }

  /**
   * The reference node that is created on first access.
   *
   */
  def referenceNode = {
    val referenceProperty = "6b67f6429706419098b4f02923a5a9d5"
    withTx {
      vertex(referenceProperty := 0)(graph)
    }
  }

  /**
   * Uses blueprints [[com.tinkerpop.blueprints.util.io.graphml.GraphMLWriter]] to
   * export this graph database into graphML.
   *
   * @param out
   */
  def exportToGraphML(out: OutputStream) {
    val writer = new GraphMLWriter(graph)
    writer.outputGraph(out)
  }

  /**
   * Uses blueprints [[com.tinkerpop.blueprints.util.io.graphml.GraphMLReader]] to
   * read the graphML input stream into this database.
   *
   * @param in input stream containing GraphML data
   */
  def importGraphML(in: InputStream) {
    val reader = new GraphMLReader(graph)
    reader.inputGraph(in)
  }
}
