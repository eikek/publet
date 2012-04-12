package org.eknet.publet.engine.scalascript

import org.eknet.publet.engine.convert.ConverterEngine
import org.eknet.publet.resource.ContentType._
import org.eknet.publet.resource.{NodeContent, Content}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 12.04.12 14:13
 */

object ScalaHtmlConverter extends ConverterEngine#Converter {
  def apply(v1: Content) = {
    NodeContent(<pre><code>{ v1.contentAsString }</code></pre>, html)
  }
}
