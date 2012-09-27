package org.eknet.publet.ext.counter

import java.text.DateFormat
import java.util
import org.eknet.publet.web.PubletWeb
import org.eknet.publet.ext.ExtDb
import com.tinkerpop.blueprints.Vertex
import org.apache.shiro.crypto.hash.Md5Hash
import org.eknet.publet.vfs.{ContentResource, Path}
import org.apache.shiro.util.ByteSource
import org.apache.shiro.crypto.hash.format.HexFormat
import org.eknet.publet.web.util.ClientInfo

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
   * Returns all uris sorted by last access time, descending.
   * @return
   */
  def getUrisByAccess: List[(String, Long)]

  /**
   * Returns all uris sorted by access count, descending.
   *
   * @return
   */
  def getUrisByCount: List[(String, Long)]

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
   * Returns the md5 has of the resource at the specified uri. If no resource
   * is found, [[scala.None]] is returned.
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

    def getUrisByAccess: List[(String, Long)] = {
      db.withTx {
        db.graph.getVertices.filter(v => v.getProperty(pagePathKey) != null).toList.sortWith((v1, v2) => {
          val l0 = v1.getProperty(pageLastAccessKey).asInstanceOf[Long]
          val l1 = v2.getProperty(pageLastAccessKey).asInstanceOf[Long]
          l0.compareTo(l1) > 0
        }).map(v => (
          v.getProperty(pagePathKey).asInstanceOf[String],
          v.getProperty(pageLastAccessKey).asInstanceOf[Long])
        )
      }
    }

    def getUrisByCount: List[(String, Long)] = {
      db.withTx {
        db.graph.getVertices.filter(v => v.getProperty(pagePathKey) != null).toList.sortWith((v1, v2) => {
          val l0 = v1.getProperty(pageCountKey).asInstanceOf[Long]
          val l1 = v2.getProperty(pageCountKey).asInstanceOf[Long]
          l0.compareTo(l1) > 0
        }).map(v => (
          v.getProperty(pagePathKey).asInstanceOf[String],
          v.getProperty(pageCountKey).asInstanceOf[Long])
        )
      }
    }

    private def getOrCreatePageVertex(uriPath: String) = db.graph.getVertices(pagePathKey, uriPath).headOption getOrElse {
      val pv = db.graph.addVertex()
      pv.setProperty(pagePathKey, uriPath)
      pv.setProperty(pageCountKey, 0L)
      pv
    }

    def collect(uri: String, info: ClientInfo) {
      def isBlacklisted: Boolean = {
        //dont count spiders...
        val bot = info.agent.map(_.toLowerCase)
          .exists(agent => agent.contains("spider") || agent.contains("bot"))

        //honor blacklist in settings
        lazy val bl = PubletWeb
          .publetSettings("ext.counter.blacklist."+ info.ip)
          .map(_.toBoolean).getOrElse(false)

        bot || bl
      }
      if (!isBlacklisted) {
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
      //property for storing the last mod date in order to update the checksum if necessary
      val lastmod = "__page_lastmod"
      //normalize uri, it is saved without a starting `/` in the graph
      val uriPath = if (uri.startsWith("/")) uri.substring(1) else uri

      /** Create a checksum of the inputstream of the resource */
      def createChecksum(res: ContentResource): (String, Long) = {
        val source = ByteSource.Util.bytes(res.inputStream)
        val md5 = new Md5Hash(source, null)
        val format = new HexFormat
        (format.format(md5), res.lastModification.getOrElse(0L))
      }

      /** Updates the checksum property with a new checksum of the given resource */
      def updateChecksum(pv: Vertex, res: ContentResource): String = {
        val cs = createChecksum(res)
        pv.setProperty(pageMd5Checksum, cs._1)
        pv.setProperty(lastmod, cs._2)
        cs._1
      }

      PubletWeb.publet.findSources(Path(uriPath)).headOption map { res =>
        db.withTx {
          val pv = getOrCreatePageVertex(uriPath)
          Option(pv.getProperty(pageMd5Checksum)).map(cs => {
            val mod = Option(pv.getProperty(lastmod)).map(_.asInstanceOf[Long]).getOrElse(0L)
            val cur = PubletWeb.publet.findSources(Path(uriPath)).headOption.flatMap(_.lastModification).getOrElse(0L)
            if (cur > mod) updateChecksum(pv, res)
            else cs.asInstanceOf[String]
          }) getOrElse {
            updateChecksum(pv, res)
          }
        }
      }
    }
  }
}
