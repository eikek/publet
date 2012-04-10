package org.eknet.publet.resource

import org.eknet.publet.Path


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.04.12 21:02
 */
class RootPartition extends MountManager[Partition] {
  
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

  def children = rootMountChildren ++ mountedPaths.filter(!_.isRoot).map(p => new PathResource(this, Path("/"+ p.segments.head)))

  def rootMountChildren = resolveMount(Path.root) match {
    case Some(p) => p._2.children
    case None => List()
  }
}

object RootPartition {
  
  val id = 'root
}

private class PathResource(rp: RootPartition, val path: Path) extends ContainerResource {

  def child(name: String) = rp.lookup(path / name).get

  def parent = if (path.isRoot) None else Some(new PathResource(rp, path.parent))

  def lastModification = Some(System.currentTimeMillis())

  def name = path.segments.last

  def isRoot = path.isRoot

  def isWriteable = false

  def exists = true

  def delete() {
    error("Resource not available")
  }

  def create() {
    error("Resource not available")
  }

  def children = rp.nextSegments(path).map(s => new PathResource(rp, path.child(s)))

  def content(name: String) = childResource(name, _.content(name))

  def container(name: String) = childResource(name, _.container(name))
  
  private def childResource[T <: Resource: Manifest](name: String, createResource: Partition=>T): T = {
    rp.lookup(path / name) match {
      case Some(r) => r.asInstanceOf[T]
      case None => {
        rp.resolveMount(path / name) match {
          case None => error("No mount point for: "+ path)
          case Some(part) => createResource(part._2)
        }
      }
      case _ => error("unreachable code path")
    }
  }

  def hasEntry(name: String) = rp.nextSegments(path).contains(name)

  override def toString = "Virtual["+path+"]"
  
}

private class RootResource(rp: RootPartition) extends PathResource(rp, Path.root) {
  override def children: Iterable[_ <: Resource] = rp.children
  override def name = "root"
}