package org.eknet.publet.engine

import org.eknet.publet.{Data, ContentType}


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 12:43
 */
trait ConverterRegistry {

  private val idconv = (x:Data) => x

  def addConverter(s: ContentType, t: ContentType, c: ConverterEngine#Converter) {

  }

  def removeConverter(c: ConverterEngine#Converter) {

  }

  protected[engine] def converterFor(source: ContentType, target: ContentType): Option[ConverterEngine#Converter] = {
    if (source == target) Some(idconv)
    else sys.error("not implemented")
  }
}
