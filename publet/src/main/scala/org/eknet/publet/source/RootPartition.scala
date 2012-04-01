package org.eknet.publet.source

import collection.mutable.ListBuffer
import org.eknet.publet.{ContentType, FileName, Content, Path}


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 30.03.12 20:56
 */
class RootPartition extends Partition with MountManager[Partition] {

  def name = 'root

  def lookup(path: Path) = getMountAt(path).flatMap(_.lookup(path))

  /**
   * Finds resources that matches the name of the specified uri
   * but not necessarily the file extension.
   * <p>
   * For example, finds a `title.md` if a `title.html` is requested,
   * while `title.html` will be the first one on the Seq if it exists.
   * </p>
   *
   * @param path
   * @return
   */
  def findSources(path: Path): Option[Seq[Content]] = {
    Predef.ensuring(path != null, "null is illegal")
    val part = resolveMount(path).getOrElse(sys.error("No partition mounted for path: "+ path))
    val source = part._2
    val sourcePath = part._1
    val buffer = new ListBuffer[Content]

    // create a list of uris of all known extensions
    val ft = new FileName(path.strip(sourcePath))
    val urilist = ft.pathsForTarget.toSeq ++
      ContentType.all.filter(_ != ft.targetType).flatMap( _.extensions.map(ft.withExtension(_)) )
    //lookup all uris and returns list of results
    urilist.foreach (source.lookup(_).flatten( buffer.+= ))
    if (buffer.isEmpty) None else Some(buffer.toSeq)
  }

  def create(path: Path, target: ContentType) = resolveMount(path) match {
    case None => Left(new RuntimeException("no partition mounted for path: "+ path))
    case Some(part) => part._2.create(path.strip(part._1), target)
  }
}
