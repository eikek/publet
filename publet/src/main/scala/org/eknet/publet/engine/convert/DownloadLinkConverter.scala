package org.eknet.publet.engine.convert

import org.eknet.publet.vfs._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 16.04.12 21:43
 */
object DownloadLinkConverter extends ConverterEngine#Converter {
  def apply(v1: Path, v2: Content) = {
    val path = v1.withExt(v2.contentType.extensions.head).segments.last
    NodeContent(<p class="box info">Download the file: <a href={path}>{path}</a></p>, ContentType.html)
  }
}
