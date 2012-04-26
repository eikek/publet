package org.eknet.publet.engine.scalascript

import org.eknet.publet.vfs.{Path, ContentType, ContentResource}
import java.io.OutputStream

/** A resource that executes the given script on each access.
 *
 * Note, that the script is executed by several methods. In web environments
 * you'd cache the result once per request.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 24.04.12 23:09
 */
class ScriptResource(val path: Path, val script: ScalaScript, val contentType: ContentType) extends ContentResource {

  protected def evaluate = {
    script.serve()
      .ensuring(_.contentType == contentType, "content type mismatch")
  }

  override def lastModification = evaluate.lastModification

  def inputStream = evaluate.inputStream

  val exists = true

  override def copyTo(out: OutputStream) {
    evaluate.copyTo(out)
  }

  override def contentAsString = evaluate.contentAsString

  def parent = None

  override def toString = "Script:"+path
}

