package org.eknet.publet.ext

import org.eknet.publet.ext.orient.OrientDb
import com.tinkerpop.blueprints.Vertex

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.06.12 23:46
 */
object ExtDb extends OrientDb(OrientDb.newGraph("extdb")) {


  /**
   * Node where all nodes that represent pages/resources
   * are connected to.
   *
   */
  val pagesNode = addReferenceVertex("pages")

  graph.createKeyIndex(Property.pagePathKey, classOf[Vertex])

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
