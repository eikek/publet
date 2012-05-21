package org.eknet.publet.web.util

import org.eknet.publet.vfs.Content
import org.fusesource.scalate.TemplateSource
import org.eknet.publet.web.PubletWeb

/**
 * Utility methods for [[org.eknet.publet.engine.scala.ScalaScript]]s
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 21.05.12 08:46
 *
 */
object RenderUtils {

  def renderTemplate(uri: String, attributes: Map[String, Any]): Option[Content] = {
    val attr = PubletWeb.scalateEngine.attributes ++ attributes
    Some(PubletWeb.scalateEngine.processUri(uri, attr))
  }

  def renderTemplate(source: TemplateSource, attributes: Map[String, Any]): Option[Content] = {
    val attr = PubletWeb.scalateEngine.attributes ++ attributes
    Some(PubletWeb.scalateEngine.processSource(source, attr))
  }

}
