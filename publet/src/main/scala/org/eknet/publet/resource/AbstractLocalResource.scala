package org.eknet.publet.resource

import java.io.File
import org.eknet.publet.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.04.12 20:44
 */
abstract class AbstractLocalResource(val file: File, root: Path) extends Resource {

  def path = Path(file).strip(root)

  def parent = if (isRoot) None else Some(new DirectoryResource(file.getParentFile, root))

  def lastModification = Some(file.lastModified())

  def name = file.getName

  def isRoot = path.isRoot

  def isWriteable = file.canWrite

  def exists = file.exists()

  def delete() {
    file.delete()
  }

}
