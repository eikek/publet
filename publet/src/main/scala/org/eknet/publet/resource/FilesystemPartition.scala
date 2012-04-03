package org.eknet.publet.resource

import java.io.File
import org.eknet.publet.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.04.12 22:53
 */
class FilesystemPartition(val root: File, val id: Symbol) extends Partition {
  
  Predef.ensuring(root.exists(), "root directory must exist")
  Predef.ensuring(root.isDirectory, "root must be a directory")
  
  def this(root: String, id: Symbol) = this(new File(root), id)

  def lookup(path: Path) = {
    val r = resourceFrom(new File(root, path.segments.mkString(File.separator)))
    if (r.exists) Some(r) else None
  }

  def children = root.listFiles().map(resourceFrom)

  def createContent(path: Path) = {
    val r = fileFrom(path)
    if (r.exists) sys.error("Resource already exists")
    r.create(); r
  }

  def createContainer(path: Path) = {
    val r = directoryFrom(path)
    if (r.exists) sys.error("Resource already exists")
    r.create(); r
  }
  
  private def resourceFrom(f: File) = if (f.isDirectory)
    new DirectoryResource(f, root) else
    new FileResource(f, root)
  
  private def fileFrom(p: Path) = new FileResource(
    new File(root, p.segments.mkString(File.separator)), root)

  private def directoryFrom(p: Path) = new DirectoryResource(
    new File(root, p.segments.mkString(File.separator)), root)
}
