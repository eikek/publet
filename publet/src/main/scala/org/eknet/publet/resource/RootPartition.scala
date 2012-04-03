package org.eknet.publet.resource

import org.eknet.publet.{ContentType, Path}
import java.awt.image.renderable.ContextualRenderedImageFactory
import java.io.{FileOutputStream, OutputStream}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.04.12 21:02
 */
class RootPartition extends MountManager[Partition] with Partition {
  
  def id = RootPartition.id
  
  def lookup(path: Path) = {
    resolveMount(path) match {
      case None => {
        if (path.isRoot) Some(new RootResource(this))
        else if (pathEndsInMountpoint(path)) Some(new PathResource(this, path))
        else None
      }
      case Some(mp) => mp._2.lookup(path.strip(mp._1))
    }
  }

  def children = mountedPaths.map(p => new PathResource(this, Path("/"+ p.segments.head)))

  def createContent(path: Path) = null

  def createContainer(path: Path) = null
}

object RootPartition {
  
  val id = 'root
}

private class PathResource(rp: RootPartition, val path: Path) extends ContainerResource {

  def child(name: String) = rp.lookup(path / name)

  def parent = if (path.isRoot) None else Some(new PathResource(rp, path.parent))

  def lastModification = Some(System.currentTimeMillis())

  def name = path.segments.last

  def isRoot = path.isRoot

  def isWriteable = false

  def exists = true

  def delete() {
    sys.error("Resource not available")
  }

  def create() {
    sys.error("Resource not available")
  }

  def children = rp.nextSegments(path).map(s => new PathResource(rp, path.child(s)))

  def content(name: String) = childResource(name, _.createContent(path / name))

  def container(name: String) = childResource(name, _.createContainer(path / name))
  
  private def childResource[T <: Resource: Manifest](name: String, createResource: Partition=>T): T = {
    rp.lookup(path / name) match {
      case Some(r) => r.asInstanceOf[T]
      case None => {
        rp.resolveMount(path / name) match {
          case None => sys.error("No mount point for: "+ path)
          case Some(part) => createResource(part._2)
        }
      }
      case _ => sys.error("unreachable code path")
    }
  }
}

private class RootResource(rp: RootPartition) extends PathResource(rp, Path.root) {
  override def children: Iterable[_ <: Resource] = rp.children
  override def name = "root"
}