package org.eknet.publet.source

import org.eknet.publet.Content._
import org.eknet.publet.{Content, Path}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 31.03.12 12:20
 */
class ClasspathPartition(root: Path, val name: Symbol) extends Partition {

  def this(root: Path) = this(root, 'classpath)

  def lookup(path: Path) = {
    val p = root / path
    val url = getClass.getResource(p.asString)
    if (url == null)
      None
    else
      Some(Content(url))
  }

}
