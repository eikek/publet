package org.eknet.publet.web

import scala.collection.JavaConverters._
import javax.servlet.ServletContext

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 20:40
 */
class DefaultWebPublet(sc: ServletContext, exts: java.util.List[WebExtension]) extends WebPublet {

  private val triple = PubletFactory.createPublet()

  exts.asScala.foreach(_.onStartup(this, sc))

  def publet = triple._1

  def gitPartition = triple._2

  def standardEngine = triple._3
}
