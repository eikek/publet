package org.eknet.publet.vfs.fs

import java.io.File
import org.eknet.publet.vfs._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.04.12 20:44
 */
abstract class AbstractLocalResource(val file: File, val rootPath: Path) extends Resource with FileResourceFactory {

  def name = if (file.isDirectory) ResourceName(file.getName+"/") else ResourceName(file.getName)

  def parent = if (Path(file.getAbsolutePath).size == rootPath.size) None else Some(newDirectory(file.getParentFile, rootPath))

  def exists = file.exists()

  def delete() {
    file.delete()
  }

  def lastModification = Some(file.lastModified())

}
