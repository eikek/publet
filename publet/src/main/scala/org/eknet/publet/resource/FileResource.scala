package org.eknet.publet.resource

import java.io.{FileOutputStream, FileInputStream, File}
import org.eknet.publet.{ContentType, Path}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 14:06
 */
class FileResource(f: File, root: Path) extends AbstractLocalResource(f, root) with ContentResource {

  def inputStream = new FileInputStream(file)

  def outputStream = Some(new FileOutputStream(file))

  def create() {
    file.createNewFile()
  }

  def length = Some(file.length())

  def contentType = ContentType(f)

  override def toString = "File["+ f.toString +"]"
}
