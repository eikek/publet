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
      case None => Right(false)
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
}


