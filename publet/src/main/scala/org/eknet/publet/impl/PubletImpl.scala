package org.eknet.publet.impl

import org.eknet.publet.impl.Conversions._
import org.eknet.publet._
import engine.{PubletEngine, EngineResolver}
import resource._

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
      case Nil => Right(None)
      //lookup the engine according to the uri scheme and process data
      case data => engine.process(path, data, target)
    }
  }

  def push(path: Path, content: Content): Either[Exception, Boolean] = {
    findSources(path) match {
      case Nil => create(path, content.contentType); push(path, content)
      case c => {
        if (c.head.isWriteable) {
          c.head.writeFrom(content.inputStream)
          Right(true)
        }
        else Left(new RuntimeException("Resource not writeable"))
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
  def findSources(path: Path): Seq[ContentResource] = {
    Predef.ensuring(path != null, "null is illegal")
    val part = resolveMount(path).getOrElse(sys.error("No partition mounted for path: "+ path))
    val source = part._2
    val sourcePath = part._1

    // create a list of uris of all known extensions
    val ft = new FileName(path.strip(sourcePath))
    val urilist = ft.pathsForTarget ++
      ContentType.all.filter(_ != ft.targetType).flatMap( _.extensions.map(ft.withExtension(_)) )
    //lookup all uris and returns list of results
    urilist.map(source.lookup).filter(o => o.isDefined && o.get.isInstanceOf[ContentResource])
      .map(or => or.get.asInstanceOf[ContentResource]).toSeq
  }

  def create(path: Path, contentType: ContentType) = createContent(path.withExtension(contentType.extensions.head))

  def children(path: Path) = {
    lookup(path) match {
      case None => List()
      case Some(r) => r match {
        case cr: ContainerResource => cr.children
        case _ => List()
      }
    }
  }
}


