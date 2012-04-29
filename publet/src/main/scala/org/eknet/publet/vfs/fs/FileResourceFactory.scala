package org.eknet.publet.vfs.fs

import java.io.File
import org.eknet.publet.vfs.{ContentResource, ContainerResource, Path}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.04.12 01:18
 */
trait FileResourceFactory {


  protected def newDirectory(f: File, root: Path): ContainerResource = new DirectoryResource(f, root)

  protected def newFile(f: File, root: Path): ContentResource = new FileResource(f, root)

}
