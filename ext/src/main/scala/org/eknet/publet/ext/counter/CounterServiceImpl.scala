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

import java.util.concurrent.TimeUnit
import org.eknet.publet.web.util.{StringMap, ClientInfo}
import org.eknet.publet.{Publet, Glob}
import org.eknet.publet.vfs.{Path, ContentResource}
import org.apache.shiro.util.ByteSource
import org.apache.shiro.crypto.hash.Md5Hash
import org.apache.shiro.crypto.hash.format.HexFormat
import com.tinkerpop.blueprints.Vertex
import java.util.Locale
import java.text.DateFormat
import java.util
import collection.JavaConversions._
import com.google.inject.name.Named
import com.google.inject.{Singleton, Inject}
import com.google.common.eventbus.Subscribe
import org.eknet.publet.ext.orient.{RichVertex, GraphDbProvider}
import org.eknet.publet.web.SettingsReloadedEvent

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.10.12 02:49
 */
@Singleton
class CounterServiceImpl @Inject() (@Named("settings") settings: StringMap, dbprovider: GraphDbProvider, publet: Publet) extends CounterService {

  private val ipBlacklist = new IpBlacklist(settings, (15, TimeUnit.HOURS))
  private val db = dbprovider.getDatabase("extdb")
  private implicit val graph = db.graph
  import org.eknet.publet.ext.orient.GraphDsl._
  import Property._
  import Label._

  /**
   * Node where all nodes that represent pages/resources
   * are connected to.
   *
   */
  val pagesNode = vertex("pages", "pages", {v =>
    db.referenceNode --> "pages" --> v
    db.graph.createKeyIndex(Property.pagePathKey, classOf[Vertex])
  })


  @Subscribe
  def reloadIps(event: SettingsReloadedEvent) {
    ipBlacklist.reloadIps()
  }

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
      vertices(pagePathKey, uriPath)
        .headOption
        .map(_.getProperty(pageLastAccessKey).asInstanceOf[Long])
        .getOrElse(0L)
    }
  }

  def getUrisByAccess: List[(String, Long)] = {
    db.withTx {
      vertices.filter(v => v(pagePathKey) != null).toList.sortWith((v1, v2) => {
        val l0 = v1(pageLastAccessKey).asInstanceOf[Long]
        val l1 = v2(pageLastAccessKey).asInstanceOf[Long]
        l0.compareTo(l1) > 0
      }).map(v => (
        v(pagePathKey).asInstanceOf[String],
        v(pageLastAccessKey).asInstanceOf[Long])
      )
    }
  }

  def getUrisByCount: List[(String, Long)] = {
    db.withTx {
      vertices.filter(v => v(pagePathKey) != null).toList.sortWith((v1, v2) => {
        val l0 = v1(pageCountKey).asInstanceOf[Long]
        val l1 = v2(pageCountKey).asInstanceOf[Long]
        l0.compareTo(l1) > 0
      }).map(v => (
        v(pagePathKey).asInstanceOf[String],
        v(pageCountKey).asInstanceOf[Long])
      )
    }
  }

  def getLastAccessString(uri: String, locale: Option[Locale]) = {
    val df = DateFormat.getDateTimeInstance(
      DateFormat.MEDIUM,
      DateFormat.MEDIUM,
      locale.getOrElse(util.Locale.getDefault)
    )
    df.format(getLastAccess(uri))
  }

  private def pageVertex(uriPath: String) = vertex(pagePathKey, uriPath, v => v(pageCountKey) = Long.box(0))

  def collect(uri: String, info: ClientInfo) {
    def isBlacklisted: Boolean = {
      //dont count spiders...
      val bot = info.agent.map(_.toLowerCase)
        .exists(agent => agent.contains("spider") || agent.contains("bot"))

      //honor blacklist in settings
      lazy val bl = ipBlacklist.isListed(info.ip)

      bot || bl
    }
    val urlmatch = settings("ext.counter.pattern")
      .map(Glob(_).matches(uri)).getOrElse(true)
    if (!isBlacklisted && urlmatch) {
      val uriPath = if (uri.startsWith("/")) uri.substring(1) else uri
      db.withTx {
        val pvertex = pageVertex(uriPath)
        val count: Long = pvertex.get(pageCountKey)
        pvertex(pageCountKey) = count +1
        pvertex(pageLastAccessKey) = System.currentTimeMillis()
        pagesNode --> pageEdgeLabel --> pvertex
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
      pv(pageMd5Checksum) = cs._1
      pv(lastmod) = cs._2
      cs._1
    }

    publet.findSources(Path(uriPath)).headOption map { res =>
      db.withTx {
        val pv = pageVertex(uriPath)
        Option(pv(pageMd5Checksum)).map(cs => {
          val mod = Option(pv(lastmod)).map(_.asInstanceOf[Long]).getOrElse(0L)
          val cur = publet.findSources(Path(uriPath)).headOption.flatMap(_.lastModification).getOrElse(0L)
          if (cur > mod) updateChecksum(pv, res)
          else cs.asInstanceOf[String]
        }) getOrElse {
          updateChecksum(pv, res)
        }
      }
    }
  }

  object Label {

    /** The label of the edge from the reference node to each uri node */
    val pageEdgeLabel = "page"

  }

  object Property {

    /** The property key of the uri value */
    val pagePathKey = "page_pagePath"

    /** The property key of the count value */
    val pageCountKey = "page_accessCount"

    /** The property key of the last access time value */
    val pageLastAccessKey = "page_lastAccess"

    /** The property key of the md5 checksum of a resource */
    val pageMd5Checksum = "page_checksum"

  }
}
