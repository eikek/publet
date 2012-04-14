package org.eknet.publet.resource

import java.io.File
import org.eknet.publet.Path
import org.eknet.publet.impl.Conversions._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.04.12 22:53
 */
class FilesystemPartition(val root: File, val id: Symbol, create:Boolean = true) extends Partition {
  
  if (!root.exists()) root.mkdirs()

  Predef.ensuring(root.exists(), "root directory must exist")
  Predef.ensuring(root.isDirectory, "root must be a directory")
  
  def this(root: String, id: Symbol) = this(new File(root), id)

  def lookup(path: Path) = {
    val f = new File(root, path.segments.mkString(File.separator))
    if (f.exists()) Some(resourceFrom(f))
    else None
  }

  def children = root.listFiles().map(resourceFrom)

  def newContainer(path: Path) = directoryFrom(path)

  def newContent(path: Path) = fileFrom(path)

  def content(name: String) = new FileResource(new File(root, name), root)

  def container(name: String) = new DirectoryResource(new File(root, name), root)

  def child(name: String) = resourceFrom(new File(root, name))

  def hasEntry(name: String) = new File(root, name).exists()

  private def resourceFrom(f: File) = if (!f.exists()) throwException("File does not exist.")
    else if (f.isDirectory)
      new DirectoryResource(f, root) else
      new FileResource(f, root)
  
  private def fileFrom(p: Path) = new FileResource(
    new File(root, p.segments.mkString(File.separator)), root)

  private def directoryFrom(p: Path) = new DirectoryResource(
    new File(root, p.segments.mkString(File.separator)), root)
}
