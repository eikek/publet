package org.eknet.publet.partition.git

import java.io.File
import org.eknet.publet.vfs.{Path, Resource}
import org.eknet.publet.vfs.fs.DirectoryResource

class GitDirectory(dir: File, root: Path, gp: GitPartition) extends DirectoryResource(dir, root) {

  override protected def newDirectory(f: File, root: Path) = GitPartition.newDirectory(f, root, gp)

  override protected def newFile(f: File, root: Path) = GitPartition.newFile(f, root, gp)

  override def children: Iterable[_ <: Resource] = super.children.filterNot(_.path.asString.startsWith("/.git"))

}
