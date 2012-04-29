package org.eknet.publet.engine.scala

import java.io.OutputStream
import org.eknet.publet.vfs.{ResourceName, ContentResource}

/**A resource that executes the given script on each access.
 *
 * Note, that the script is executed by almost all methods (for
 * example to get the content type). You need to implement the
 * desired caching strategy. In web environments you'd cache
 * the result per request.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 24.04.12 23:09
 */
abstract class ScriptResource(val name: ResourceName, val script: ScalaScript) extends ContentResource {

  protected def evaluate = script.serve().get

  override def lastModification = evaluate.lastModification

  def inputStream = evaluate.inputStream

  def contentType = evaluate.contentType

  val exists = true

  override def copyTo(out: OutputStream) {
    evaluate.copyTo(out)
  }

  override def contentAsString = evaluate.contentAsString

  override def toString = "Script:" + name
}

