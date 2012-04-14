package org.eknet.publet.resource

import org.eknet.publet.Path
import java.io.File
import org.eknet.publet.impl.Conversions._

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 14:07
 */
class DirectoryResource(dir: File, root: Path) extends AbstractLocalResource(dir, root) with ContainerResource {

  def children = dir.listFiles().map(f => {
    if (f.isDirectory) new DirectoryResource(f, root)
    else new FileResource(f, root)
  })

  def content(name: String) = new FileResource(new File(dir, name), root)

  def container(name: String) = new DirectoryResource(new File(dir, name), root)

  def child(name: String) = {
    val f = new File(dir, name)
    if (!f.exists) throwException("Child does not exist")
    else if (f.isDirectory) new DirectoryResource(f, root)
         else new FileResource(f, root)
  }

  def create() {
    dir.mkdir()
  }

  def hasEntry(name: String) = new File(dir, name).exists()

  override def toString = "Directory["+ dir.toString +"]"
}
