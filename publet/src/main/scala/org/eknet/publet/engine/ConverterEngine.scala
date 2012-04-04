package org.eknet.publet.engine

import org.eknet.publet.resource.{ContentType, Content}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 10:11
 */
trait ConverterEngine extends PubletEngine {

  type Converter = Content => Content

  def addConverter(mapping: (ContentType, ContentType), c: ConverterEngine#Converter)

}

object ConverterEngine {

  def apply(): DefaultConverterEngine = new DefaultConverterEngine

}