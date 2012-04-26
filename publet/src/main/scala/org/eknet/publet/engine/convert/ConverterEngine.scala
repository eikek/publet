package org.eknet.publet.engine.convert

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.vfs.{Path, ContentType, Content}
import org.eknet.publet.vfs.ContentType._

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

  def apply(name: Symbol): ConverterEngine = {
    val convEngine = new DefaultConverterEngine(name)
    convEngine.addConverter(markdown -> html, KnockoffConverter)
    convEngine.addConverter(scal -> html, CodeHtmlConverter.scala)
    convEngine.addConverter(css -> html, CodeHtmlConverter.css)
    convEngine.addConverter(javascript -> html, CodeHtmlConverter.json)
    convEngine.addConverter(text -> html, CodeHtmlConverter.json)
    convEngine.addConverter(xml -> html, CodeHtmlConverter.json)
    convEngine.addConverter(png -> html, ImageHtmlConverter)
    convEngine.addConverter(jpg -> html, ImageHtmlConverter)
    convEngine.addConverter(gif -> html, ImageHtmlConverter)
    convEngine.addConverter(icon -> html, ImageHtmlConverter)
    convEngine.addConverter(pdf -> html, DownloadLinkConverter)
    convEngine.addConverter(zip -> html, DownloadLinkConverter)
    convEngine.addConverter(unknown -> html, DownloadLinkConverter)
    convEngine
  }

  def appy(): ConverterEngine = ConverterEngine('include)

  def compose(c1: ConverterEngine#Converter, c2: ConverterEngine#Converter) = (p:Path, c:Content) => c1(p, c2(p,c))
}