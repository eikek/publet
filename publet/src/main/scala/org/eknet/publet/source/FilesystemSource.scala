package org.eknet.publet.source

import java.net.URI
import org.eknet.publet.Data
import tools.nsc.io.{File, Directory}
import java.nio.file.{Paths, Path, StandardCopyOption, Files => JFiles}
import org.eknet.publet.impl.Conversions._

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

  def lookup(uri: URI) = {
    val file = relativeFile(uri)
    if (file.exists) Option(Data(file)) else None
  }

  def push(uri: URI)(data: Data) {
    val file = relativeFile(uri)
    if (!file.exists) file.parent.createDirectory(force = true, failIfExists = true)

    val path: Path = Paths.get(file.toURI)
    JFiles.copy(data.content, path, StandardCopyOption.REPLACE_EXISTING)
  }

  private def relativeFile(uri: URI) = File(root / uri.validPath)
}
