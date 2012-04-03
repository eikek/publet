package org.eknet.publet.impl

import org.eknet.publet.impl.Conversions._
import collection.mutable.ListBuffer
import org.eknet.publet._
import engine.{PubletEngine, EngineResolver}
import resource.{ContentResource, RootPartition}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:43
 */
class PubletImpl extends RootPartition with Publet with EngineResolver {

  def process(path: Path): Either[Exception, Option[Content]] = {
    Predef.ensuring(path != null, "null is illegal")
    process(path, path.targetType.get)
  }

  def process(path: Path, target: ContentType) = {
    //lookup engine for uri pattern
    val engine = resolveEngine(path)
      .getOrElse(sys.error("No engine found for uri: "+ path))

    process(path, target, engine)
  }

  def process(path: Path, target: ContentType, engine: PubletEngine): Either[Exception, Option[Content]] = {
    // lookup the source
    findSources(path) match {
      case None => Right(None)
      //lookup the engine according to the uri scheme and process data
      case Some(data) => engine.process(path, data, target)
    }
  }

  def push(path: Path, content: Content): Either[Exception, Boolean] = {
    findSources(path) match {
      case None => create(path, content.contentType); push(path, content)
      case Some(c) => c.head.output match {
        case None => Left(sys.error("not writeable"))
        case Some(out) => try {
          content.copyTo(out)
          Right(true)
        } catch {
          case e: Exception => Left(e)
        }
      }
    }
  }

  override def addEngine(engine: PubletEngine) {
    engine match {
      case e: InstallCallback => e.onInstall(this)
      case _ => ()
    }
    super.addEngine(engine)
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
    urilist.map(source.lookup).filter(o => o.isDefined && o.get.isInstanceOf[ContentResource])
      .map(or => or.get.asInstanceOf[ContentResource].toContent).flatten(buffer.+=)
    if (buffer.isEmpty) None else Some(buffer.toSeq)
  }

  def create(path: Path, contentType: ContentType) = createContent(path.withExtension(contentType.extensions.head))

}


