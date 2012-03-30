package org.eknet.publet.source

import tools.nsc.io.{File, Directory}
import org.eknet.publet.{Path, Page}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 23:41
 */
class FilesystemPartition(root: Directory) extends Partition {
  
  Predef.ensuring(root != null, "dir must not be null")
  Predef.ensuring(root.isValid, "Not a directory")

  if (!root.exists) root.createDirectory(force = true, failIfExists = true)
  
  def this(path: Path) = this(Directory(path.toAbsolute.asString))
  def this(str: String) = this(Path(str))

  def name = 'local

  def lookup(path: Path) = {
    val file = relativeFile(path)
    if (file.exists) Option(Page(file)) else None
  }

//  def push(uri: Uri)(data: Page) {
//    val file = relativeFile(uri)
//    if (!file.exists) file.parent.createDirectory(force = true, failIfExists = true)
//    val path: Path = Paths.get(file.toURI)
//    JFiles.copy(data.content, path, StandardCopyOption.REPLACE_EXISTING)
//  }

  private def relativeFile(path: Path):File = (Path(root) / path)
}
