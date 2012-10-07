/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet.ext.counter

import org.eknet.publet.ext.ExtDb
import org.eknet.publet.web.PubletWeb
import java.util.concurrent.TimeUnit
import org.eknet.publet.web.util.ClientInfo
import org.eknet.publet.Glob
import org.eknet.publet.vfs.{Path, ContentResource}
import org.apache.shiro.util.ByteSource
import org.apache.shiro.crypto.hash.Md5Hash
import org.apache.shiro.crypto.hash.format.HexFormat
import com.tinkerpop.blueprints.Vertex

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.10.12 02:49
 */
class CounterServiceImpl extends CounterService {

  import collection.JavaConversions._
  import ExtDb.Property._

  private val ipBlacklist = new IpBlacklist(PubletWeb.publetSettings, (15, TimeUnit.HOURS))
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
      lazy val bl = ipBlacklist.isListed(info.ip)

      bot || bl
    }
    val urlmatch = PubletWeb.publetSettings("ext.counter.pattern")
      .map(Glob(_).matches(uri)).getOrElse(true)
    if (!isBlacklisted && urlmatch) {
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
