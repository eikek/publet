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
import org.eknet.publet.web.util.ClientInfo
import org.eknet.publet.Publet
import org.eknet.publet.vfs.{Path, ContentResource}
import com.tinkerpop.blueprints.Vertex
import java.util.Locale
import java.text.DateFormat
import java.util
import com.google.inject.{Singleton, Inject}
import com.google.common.eventbus.Subscribe
import org.eknet.publet.web.{Settings, SettingsReloadedEvent}
import org.eknet.publet.ext.graphdb.GraphDbProvider
import org.eknet.scue._
import org.eknet.publet.ext.ResourceInfo

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.10.12 02:49
 */
@Singleton
class CounterServiceImpl @Inject() (settings: Settings, dbprovider: GraphDbProvider, publet: Publet) extends CounterService with CounterManagerMBean {

  private val ipBlacklist = new IpBlacklist(settings)
  private val urlBlacklist = new UrlBlacklist(settings)
  private val db = dbprovider.getDatabase("extdb")
  private implicit val graph = db.graph
  import GraphDsl._
  import Properties._
  import Labels._

  /**
   * Node where all nodes that represent pages/resources
   * are connected to.
   *
   */
  def pagesNode = withTx {
    vertex("name" := "pages", {v =>
      db.graph.createKeyIndex(Properties.pagePathKey, classOf[Vertex])
    })
  }


  @Subscribe
  def reloadIps(event: SettingsReloadedEvent) {
    ipBlacklist.reloadIps()
    urlBlacklist.reloadUrlList()
  }

  def getPageCount(uri: String) = {
    val uriPath = if (uri.startsWith("/")) uri.substring(1) else uri
    db.withTx {
      vertices(pagePathKey, uriPath)
        .headOption
        .flatMap(_.get(pageCountKey))
        .getOrElse(0L)
    }
  }

  def setPageCount(uri: String, value: Long) {
    val uriPath = if (uri.startsWith("/")) uri.substring(1) else uri
    db.withTx {
      vertices(pagePathKey, uriPath)
        .headOption
        .map(v => v(pageCountKey) = value)
    }
  }

  def getLastAccess(uri: String) = {
    val uriPath = if (uri.startsWith("/")) uri.substring(1) else uri
    db.withTx{
      vertices(pagePathKey, uriPath)
        .headOption
        .flatMap(_.get(pageLastAccessKey))
        .getOrElse(0L)
    }
  }

  def getUrisByAccess: List[(String, Long)] = {
    db.withTx {
      vertices.filter(v => v(pagePathKey).isDefined).toList.sortWith((v1, v2) => {
        val l0: Long = v1.get(pageLastAccessKey).getOrElse(0)
        val l1: Long = v2.get(pageLastAccessKey).getOrElse(0)
        l0.compareTo(l1) > 0
      }).map(v => (
        v.get[String](pagePathKey).get,
        v.get[Long](pageLastAccessKey).getOrElse(0L)
      ))
    }
  }

  def getUrisByCount: List[(String, Long)] = {
    db.withTx {
      vertices.filter(v => v(pagePathKey).isDefined).toList.sortWith((v1, v2) => {
        val l0: Long = v1.get(pageCountKey).getOrElse(0)
        val l1: Long = v2.get(pageCountKey).getOrElse(0)
        l0.compareTo(l1) > 0
      }).map(v => (
        v(pagePathKey).get.asInstanceOf[String],
        v(pageCountKey).getOrElse(0L).asInstanceOf[Long])
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

  private def pageVertex(uriPath: String) = vertex(pagePathKey := uriPath, v => pagesNode --> pageEdgeLabel --> v)

  def collect(uri: String, info: ClientInfo) {
    def isBlacklisted: Boolean = {
      //dont count spiders...
      val bot = info.agent.map(_.toLowerCase)
        .exists(agent => agent.matches(".*crawler.*|.*spider.*|.*bot.*"))

      //honor blacklist in settings
      lazy val ipListed = ipBlacklist.isListed(info.ip)
      lazy val urlListed = urlBlacklist.isListed(uri)

      bot || ipListed || urlListed
    }
    if (!isBlacklisted) {
      val uriPath = if (uri.startsWith("/")) uri.substring(1) else uri
      db.withTx {
        val pvertex = pageVertex(uriPath)
        val count: Long = pvertex.get(pageCountKey).getOrElse(0)
        pvertex(pageCountKey) = count +1
        pvertex(pageLastAccessKey) = System.currentTimeMillis()
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
      val md5string = ResourceInfo.createMd5(res.inputStream)
      (md5string, res.lastModification.getOrElse(0L))
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
        pv(pageMd5Checksum).map(cs => {
          val mod = pv(lastmod).map(_.asInstanceOf[Long]).getOrElse(0L)
          val cur = publet.findSources(Path(uriPath)).headOption.flatMap(_.lastModification).getOrElse(0L)
          if (cur > mod) updateChecksum(pv, res)
          else cs.asInstanceOf[String]
        }) getOrElse {
          updateChecksum(pv, res)
        }
      }
    }
  }

  object Labels {

    /** The label of the edge from the reference node to each uri node */
    val pageEdgeLabel = "page"

  }

  object Properties {

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
