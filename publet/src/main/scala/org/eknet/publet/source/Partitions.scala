package org.eknet.publet.source

import org.eknet.publet.Path
import java.io.File

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 31.03.12 12:24
 */
object Partitions {

  def directory(root: File) = new FilesystemPartition(root)

  def classpath(root: Path) = new ClasspathPartition(root)

  lazy val yamlPartition = classpath(Path("../themes/yaml"))

  lazy val highlightPartition = classpath(Path("../themes/highlight"))
}
