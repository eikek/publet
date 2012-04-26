package org.eknet.publet.vfs.fs

import java.io.File
import org.eknet.publet.vfs._
import org.slf4j.LoggerFactory

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.04.12 22:53
 */
class FilesystemPartition(val path: Path, val parent: Option[Container], val root: File, createDir: Boolean = true)
  extends Container with Modifyable {

  private val log = LoggerFactory.getLogger(classOf[FilesystemPartition])

  def this(root: File, createDir: Boolean = true) = this(Path.root, None, root, createDir)

  if (!root.exists() && createDir) root.mkdirs()

  if (createDir) Predef.ensuring(root.exists(), "root directory must exist")
  Predef.ensuring(root.isDirectory || !root.exists(), "root must be a directory")

  override def lookup(path: Path) = {
    val f = new File(root, path.segments.mkString(File.separator))
    log.trace("Lookup file: "+ f)
    if (f.exists()) resourceFrom(f) else None
  }

  def delete() { sys.error("Cannot delete root directory.") }

  def create() {}

  def children = root.listFiles().map(resourceFrom(_).get)

  def content(name: String) = newFile(new File(root, name), root)

  def container(name: String) = newDirectory(new File(root, name), root)

  def child(name: String) = resourceFrom(new File(root, name))

  private def resourceFrom(f: File) = if (!f.exists()) None
  else if (f.isDirectory)
    Some(newDirectory(f, root))
  else
    Some(newFile(f, root))

  protected def newDirectory(f: File, root: Path): ContainerResource = new DirectoryResource(f, root)

  protected def newFile(f: File, root: Path): ContentResource = new FileResource(f, root)

//  private def fileFrom(p: Path) = newFile(
//    new File(root, p.segments.mkString(File.separator)), root)
//
//  private def directoryFrom(p: Path) = newDirectory(
//    new File(root, p.segments.mkString(File.separator)), root)
}
