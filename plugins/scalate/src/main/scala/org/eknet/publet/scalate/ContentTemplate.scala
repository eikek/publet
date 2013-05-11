package org.eknet.publet.scalate

import org.fusesource.scalate.TemplateSource
import org.eknet.publet.content.{Content, Path}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.05.13 22:03
 */
class ContentTemplate(val path: Path, val content: Content) extends TemplateSource {
  def uri = path.absoluteString
  def inputStream = content.inputStream
  def lastModified = content.lastModification.getOrElse(System.currentTimeMillis())
}
