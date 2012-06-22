package org.eknet.publet.ext.counter

import java.text.DateFormat
import java.util
import org.eknet.publet.web.util.{Context, Key}
import org.eknet.publet.web.{PubletWeb, ClientInfo, PubletWebContext}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.06.12 19:41
 */
trait CounterService {

  /**
   * Returns the number of accesses to the given path. The path
   * is starting from the context-path.
   *
   * @param uriPath
   * @return
   */
  def getPageCount(uriPath: String): Long

  /**
   * Returns the last time the given uri has been accessed.
   *
   * @param uriPath
   * @return
   */
  def getLastAccess(uriPath: String): Long

  /**
   * Returns the last time the given uri has been accessed in a standard
   * string format. The time is formatted according to the given locale
   * (or the default locale).
   *
   * @param uri
   * @param locale
   * @return
   */
  def getLastAccessString(uri: String, locale: Option[util.Locale] = None): String = {
    val df = DateFormat.getDateTimeInstance(
      DateFormat.MEDIUM,
      DateFormat.MEDIUM,
      locale.getOrElse(util.Locale.getDefault)
    )
    df.format(getLastAccess(uri))
  }

  /**
   * "Collect" the page uri and client info. This maintains
   * the page counter.
   *
   * @param uriPath
   * @param info
   */
  def collect(uriPath: String, info: ClientInfo)

  /**
   * Completely shuts down the underlying database which
   * renders this instance unusable. Any calls to this
   * instance are illegal after this method returns.
   *
   */
  def shutdown()
}

object CounterService {

  /**
   * Creates a new instance of [[org.eknet.publet.ext.counter.CounterService]].
   *
   * Note, in web env you'd get an instance by using
   * [[org.eknet.publet.ext.counter.CounterExtension.service]]
   *
   * @return
   */
  def apply(): CounterService = new Impl

  private class Impl extends CounterService {

    import collection.JavaConversions._

    val db = new CounterDb

    def shutdown() {
      db.shutdown()
    }

    def getPageCount(uriPath: String) = {
      db.withTx(graph => {
        val vertices = graph.getVertices(db.pagePathKey, uriPath)
        Option(vertices)
          .flatMap(_.headOption)
          .map(_.getProperty(db.pageCountKey).asInstanceOf[Long])
          .getOrElse(0L)
      })
    }

    def getLastAccess(uriPath: String) = {
      db.withTx(graph => {
        val vertices = graph.getVertices(db.pagePathKey, uriPath)
        Option(vertices)
          .flatMap(_.headOption)
          .map(_.getProperty(db.pageLastAccessKey).asInstanceOf[Long])
          .getOrElse(0L)
      })
    }

    def collect(uriPath: String, info: ClientInfo) {
      import db._
      import collection.JavaConversions._

      withTx(graph => {
        val pageVertex = graph.getVertices(pagePathKey, uriPath).headOption getOrElse {
          val pv = graph.addVertex()
          pv.setProperty(pagePathKey, uriPath)
          pv.setProperty(pageCountKey, 0L)
          pv
        }

        val count = pageVertex.getProperty(pageCountKey).asInstanceOf[Long]
        pageVertex.setProperty(pageCountKey, (count +1))
        pageVertex.setProperty(pageLastAccessKey, System.currentTimeMillis())
        graph.addEdge(null, referenceNode, pageVertex, pageEdgeLabel)
      })
    }

  }
}
