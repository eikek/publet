package org.eknet.publet.source

import tools.nsc.io.Directory
import org.eknet.publet.Path

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 31.03.12 12:24
 */
object Partitions {

  def directory(root: Directory) = new FilesystemPartition(root)

  def classpath(root: Path) = new ClasspathPartition(root)

  lazy val yamlPartition = classpath(Path("../themes/yaml"))

}
