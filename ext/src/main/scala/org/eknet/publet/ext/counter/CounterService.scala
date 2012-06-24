package org.eknet.publet.ext.counter

import java.text.DateFormat
import java.util
import org.eknet.publet.web.{PubletWeb, ClientInfo}
import org.eknet.publet.ext.ExtDb
import com.tinkerpop.blueprints.Vertex
import org.apache.shiro.crypto.hash.Md5Hash
import org.eknet.publet.vfs.Path
import org.apache.shiro.util.ByteSource
import org.apache.shiro.crypto.hash.format.{HexFormat, DefaultHashFormatFactory, HashFormat}

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
   * "Collect" the page uri and client info. This increments
   * the page counter and sets a new last-access timestamp.
   *
   * @param uriPath
   * @param info
   */
  def collect(uriPath: String, info: ClientInfo)

  /**
   * Returns the md5 has of the resource at the specified uri.
   *
   * @param uriPath
   * @return
   */
  def getMd5(uriPath: String): Option[String]
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
    import ExtDb.Property._

    private val db = ExtDb

    def getPageCount(uri: String) = {
      val uriPath = if (uri.startsWith("/")) uri.substring(1) else uri
      db.withTx {
        val vertices = db.graph.getVertices(pagePathKey, uriPath)
        Option(vertices)
          .flatMap(_.headOption)
          .map(_.getProperty(pageCountKey).asInstanceOf[Long])
          .getOrElse(0L)
      }
    }

    def getLastAccess(uri: String) = {
      val uriPath = if (uri.startsWith("/")) uri.substring(1) else uri
      db.withTx{
        val vertices = db.graph.getVertices(pagePathKey, uriPath)
        Option(vertices)
          .flatMap(_.headOption)
          .map(_.getProperty(pageLastAccessKey).asInstanceOf[Long])
          .getOrElse(0L)
      }
    }

    private def getOrCreatePageVertex(uriPath: String) = db.graph.getVertices(pagePathKey, uriPath).headOption getOrElse {
      val pv = db.graph.addVertex()
      pv.setProperty(pagePathKey, uriPath)
      pv.setProperty(pageCountKey, 0L)
      pv
    }

    def collect(uri: String, info: ClientInfo) {
      if (!info.agent.exists(_.contains("bot")) && !info.agent.exists(_.contains("spider"))) {
        val uriPath = if (uri.startsWith("/")) uri.substring(1) else uri
        db.withTx {
          val pageVertex = getOrCreatePageVertex(uriPath)
          import ExtDb.Label._
          val count = pageVertex.getProperty(pageCountKey).asInstanceOf[Long]
          pageVertex.setProperty(pageCountKey, (count +1))
          pageVertex.setProperty(pageLastAccessKey, System.currentTimeMillis())
          db.graph.addEdge(null, db.pagesNode, pageVertex, pageEdgeLabel)
        }
      }
    }

    def getMd5(uri: String): Option[String] = {
      val lastmod = "__page_lastmod"
      val uriPath = if (uri.startsWith("/")) uri.substring(1) else uri

      def createChecksum(): Option[(String, Long)] = {
        PubletWeb.publet.findSources(Path(uriPath))
          .headOption
          .map(res => {
            val source = ByteSource.Util.bytes(res.inputStream)
            val md5 = new Md5Hash(source, null)
            val format = new HexFormat
            (format.format(md5), res.lastModification.getOrElse(0L))
        })
      }

      def updateChecksum(pv: Vertex): Option[String] = {
        val cs = createChecksum()
        cs.foreach(c => {
          pv.setProperty(pageMd5Checksum, c._1)
          pv.setProperty(lastmod, c._2)
        })
        cs.map(_._1)
      }

      db.withTx {
        val pv = getOrCreatePageVertex(uriPath)
        Option(pv.getProperty(pageMd5Checksum)).flatMap(cs => {
          val mod = Option(pv.getProperty(lastmod)).map(_.asInstanceOf[Long]).getOrElse(0L)
          val cur = PubletWeb.publet.findSources(Path(uriPath)).headOption.flatMap(_.lastModification).getOrElse(0L)
          if (cur > mod) updateChecksum(pv)
          else Some(cs.asInstanceOf[String])
        }) orElse {
          updateChecksum(pv)
        }
      }
    }
  }
}
