package org.eknet.publet.engine.convert

import org.eknet.publet.Path
import org.eknet.publet.resource.{ContentType, NodeContent, Content}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 16.04.12 21:22
 */
object ImageHtmlConverter extends ConverterEngine#Converter {

  def apply(path: Path, cn: Content) = {
    val imgpath = path.withExtension(cn.contentType.extensions.head).segments.last
    NodeContent(<img src={ imgpath } alt=""/>, ContentType.html)
  }
}
