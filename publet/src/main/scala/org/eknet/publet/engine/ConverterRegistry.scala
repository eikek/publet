package org.eknet.publet.engine

import org.eknet.publet.{Data, ContentType}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 12:43
 */
trait ConverterRegistry {

  addConverter(ContentType.markdown, ContentType.html, Graph.idconv)
  addConverter(ContentType.html, ContentType.text, Graph.idconv)

  def addConverter(s: ContentType, t: ContentType, c: ConverterEngine#Converter) {
    Graph.add(s, t, c)
  }

  protected[engine] def converterFor(source: ContentType, target: ContentType): Option[ConverterEngine#Converter] = {
    if (source == target) Some(Graph.idconv)
    else Some(Graph.converterChain(source, target))
  }

}

protected[engine] object Graph {

  type Converter = ConverterEngine#Converter

  val idconv = (x:Data) => x
  private val converters = collection.mutable.Map[(ContentType, ContentType), Converter]()

  def add(s: ContentType, t: ContentType, c: Converter) {
    converters.put((s, t), c)
  }
  
  def converterChain(s: ContentType, e: ContentType): Converter = {
    sys.error("not implemented")
  }
}

protected[engine] class PathConverter extends ConverterEngine#Converter {
  
  def apply(v1: Data) = null
  
}
