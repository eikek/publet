package org.eknet.publet.engine.convert

import org.eknet.publet.resource.{ContentType, Content}
import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.Path

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 10:11
 */
trait ConverterEngine extends PubletEngine {

  type Converter = (Path, Content) => (Content)

  def addConverter(mapping: (ContentType, ContentType), c: ConverterEngine#Converter)

}

object ConverterEngine {

  def apply(): DefaultConverterEngine = new DefaultConverterEngine

  def compose(c1: ConverterEngine#Converter, c2: ConverterEngine#Converter) = (p:Path, c:Content) => c1(p, c2(p,c))
}