package org.eknet.publet.impl

import org.eknet.publet.impl.Conversions._
import collection.mutable.ListBuffer
import org.eknet.publet.engine.EngineResolver
import org.eknet.publet.source.RootPartition
import org.eknet.publet._

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:43
 */
protected[publet] class PubletImpl extends RootPartition with Publet with EngineResolver {


  def process(path: Path, target: ContentType) = process(path.pathsFor(target).head)

  def process(path: Path): Either[Exception, Option[Page]] = {
    Predef.ensuring(path != null, "null is illegal")

    //lookup engine for uri pattern
    val engine = resolveEngine(path)
      .getOrElse(sys.error("No engine found for uri: "+ path))
    
    // lookup the source
    findSourceFor(path) match {
      case None => Right(None)
      //lookup the engine according to the uri scheme and process data
      case Some(data) => engine.process(data, path.targetType.get)
    }
  }

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
  private def findSourceFor(path: Path): Option[Seq[Page]] = {
    val part = resolvePartition(path).get
    val source = part._2
    val sourcePath = part._1
    val buffer = new ListBuffer[Page]

    // create a list of uris of all known extensions
    val ft = new FileName(path.strip(sourcePath))
    val urilist = ft.pathsForTarget.toSeq ++
      ContentType.all.filter(_ != ft.targetType).flatMap( _.extensions.map(ft.withExtension(_)) )
    
    //lookup all uris and returns list of results
    urilist.foreach (source.lookup(_).flatten( buffer.+= ))
    if (buffer.isEmpty) None else Some(buffer.toSeq)
  }

}


