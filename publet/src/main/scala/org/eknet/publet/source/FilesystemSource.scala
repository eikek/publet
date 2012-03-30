package org.eknet.publet.source

import tools.nsc.io.{File, Directory}
import java.nio.file.{Paths, Path, StandardCopyOption, Files => JFiles}
import org.eknet.publet.{Uri, Page}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 23:41
 */
class FilesystemSource(root: Directory) extends PubletSource {
  
  Predef.ensuring(root != null, "dir must not be null")
  Predef.ensuring(root.isValid, "Not a directory")

  if (!root.exists) root.createDirectory(force = true, failIfExists = true)
  
  def this(path: String) = this(Directory(path))

  def name = 'local

  def lookup(uri: Uri) = {
    val file = relativeFile(uri)
    if (file.exists) Option(Page(file)) else None
  }

  def push(uri: Uri)(data: Page) {
    val file = relativeFile(uri)
    if (!file.exists) file.parent.createDirectory(force = true, failIfExists = true)

    val path: Path = Paths.get(file.toURI)
    JFiles.copy(data.content, path, StandardCopyOption.REPLACE_EXISTING)
  }

  private def relativeFile(uri: Uri) = File(root / uri.path)
}
