package org.eknet.publet.resource

import org.eknet.publet.Path
import java.io.{OutputStream, FileOutputStream, FileInputStream, File}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 14:06
 */
class FileResource(f: File, root: Path) extends AbstractLocalResource(f, root) with ContentResource {

  def inputStream = new FileInputStream(file)

  def outputStream:Option[OutputStream] = Some(new FileOutputStream(file))

  def create() {
    file.createNewFile()
  }

  def length = Some(file.length())

  def contentType = ContentType(f)

  override def toString = "File["+ f.toString +"]"
}
