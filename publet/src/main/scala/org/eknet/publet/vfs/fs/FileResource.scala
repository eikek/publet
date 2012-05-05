package org.eknet.publet.vfs.fs

import java.io._
import org.eknet.publet.vfs._

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 14:06
 */
class FileResource(f: File, root: Path)
  extends AbstractLocalResource(f, root) with ContentResource with Modifyable with Writeable {

  def inputStream = new BufferedInputStream(new FileInputStream(file))

  def outputStream: OutputStream = new FileOutputStream(file)

  override def lastModification = Some(file.lastModified())

  def create() {
    file.createNewFile()
  }

  override def length = Some(file.length())

  def contentType = ContentType(f)

  override def toString = "File[" + f.toString + "]"
}
