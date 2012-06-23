package org.eknet.publet.ext

import org.eknet.publet.ext.orient.OrientDb

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


}
