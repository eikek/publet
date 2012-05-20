package org.eknet.publet.engine.scalate

import org.fusesource.scalate.TemplateSource
import org.eknet.publet.vfs.{Content, Path, ContentResource}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 00:17
 */
class TemplateResource(val uri: String, val content: Content) extends TemplateSource {
  private val time = System.currentTimeMillis()

  def this(path: Path, content: Content) = this(path.asString, content)

  def inputStream = content.inputStream
  def lastModified = content.lastModification.getOrElse(time)

  override def toString = content.toString
}
