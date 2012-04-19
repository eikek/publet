package org.eknet.publet.engine.scalascript

import org.eknet.publet.Path
import java.io.OutputStream
import org.eknet.publet.resource._
import org.slf4j.LoggerFactory

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.04.12 20:56
 */
class ScriptPartition(val id: Symbol, scripts: Map[String, (ScalaScript, ContentType)]) extends AbstractResource(Path.root) with Partition with ContainerResource {
  private val log = LoggerFactory.getLogger(getClass)

  def lookup(path: Path) = {
    val script = new Script(path)
    if (script.exists) Some(script)
    else None
  }

  def newContainer(path: Path): ContainerResource = new EmptyContainer(path)

  def newContent(path: Path): ContentResource = new Script(path)

  def children = List()

  def content(name: String) = null

  def container(name: String) = null

  def child(name: String) = null

  def hasEntry(name: String) = {
    val script = new Script(Path.root / name)
    script.exists
  }

  def isRoot = true

  def parent = None

  def lastModification = None

  def exists = true

  private class Script(path: Path) extends AbstractResource(path) with ContentResource {

    private lazy val evaluated = scripts.get(path.fileName.name).get._1.serve()
      .ensuring(_.contentType == contentType, "content type mismatch")

    lazy val contentType = scripts.get(path.fileName.name).get._2

    def outputStream = throw new RuntimeException("Cannot write to this resource.")

    def lastModification = evaluated.lastModification

    def length = None

    def inputStream = evaluated.inputStream

    val exists = path.segments.size == 1 &&
      scripts.get(path.fileName.name).isDefined &&
      path.targetType.isDefined &&
      path.targetType.get == contentType

    override def copyTo(out: OutputStream) {
      evaluated.copyTo(out)
    }

    override def contentAsString = evaluated.contentAsString

    def parent = Some(ScriptPartition.this)

    override def toString = "Script:"+path
  }

}

protected abstract class AbstractResource(val path: Path) extends Resource {

  lazy val name = if (path.isRoot) path.asString else path.segments.last

  lazy val isWriteable = false

  def delete() {
    throw new RuntimeException("Cannot delete internal resource.")
  }

  def create() {
    throw new RuntimeException("Cannot create internal resource.")
  }
}
