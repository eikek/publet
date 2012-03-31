package org.eknet.publet.impl

import org.eknet.publet.impl.Conversions._
import collection.mutable.ListBuffer
import org.eknet.publet._
import engine.{PubletEngine, EngineResolver}
import source.RootPartition

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:43
 */
class PubletImpl extends RootPartition with Publet with EngineResolver {

  def process(path: Path, target: ContentType) = process(path.pathsFor(target).head)

  def process(path: Path): Either[Exception, Option[Content]] = {
    Predef.ensuring(path != null, "null is illegal")

    //lookup engine for uri pattern
    val engine = resolveEngine(path)
      .getOrElse(sys.error("No engine found for uri: "+ path))
    
    // lookup the source
    findSources(path) match {
      case None => Right(None)
      //lookup the engine according to the uri scheme and process data
      case Some(data) => engine.process(path, data, path.targetType.get)
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

  override def addEngine(engine: PubletEngine) {
    engine match {
      case e: InstallCallback => e.onInstall(this)
      case _ => ()
    }
    super.addEngine(engine)
  }
}


