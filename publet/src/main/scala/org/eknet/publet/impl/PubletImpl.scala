package org.eknet.publet.impl

import scala.collection.mutable
import org.eknet.publet.impl.Conversions._
import org.eknet.publet._
import engine.{PubletEngine, EngineMangager}
import vfs._
import java.io.InputStream

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 22:43
 */
class PubletImpl extends MountManager with Publet with EngineMangager with RootContainer {

  def process(path: Path): Option[Content] = {
    Predef.ensuring(path != null, "null is illegal")
    process(path, path.name.targetType)
  }

  def process(path: Path, target: ContentType) = {
    //lookup engine for uri pattern
    val engine = resolveEngine(path)
      .getOrElse(throwException("No engine found for uri: "+ path))

    process(path, target, engine)
  }

  def process(path: Path, target: ContentType, engine: PubletEngine): Option[Content] = {
    // lookup the source
    findSources(path) match {
      case Nil => None
      //lookup the engine according to the uri scheme and process data
      case data => engine.process(path, data.toSeq, target)
    }
  }

  def push(path: Path, content: InputStream) {
    def copy(ext: Option[String]) {
      createResource(path, ext) match {
        case cr: ContentResource => cr.writeFrom(content)
        case r@_ => sys.error("Cannot create content for resource: "+ r)
      }
    }
    findSources(path).toList match {
      case Nil => copy(None)
      case c::cs => {
        if (c.name.ext == path.name.ext) {
          copy(Some(c.name.ext))
        } else {
          Resource.toModifyable(c).map(_.delete()).getOrElse(sys.error("Resource not modifyable"))
          copy(Some(path.name.ext))
        }
      }
    }
  }


  def delete(path: Path) {
    findSources(path).toList map { c =>
      Resource.toModifyable(c).map(_.delete()).getOrElse(sys.error("Resource not modifyable"))
    }
  }

  def findSources(path: Path): Iterable[ContentResource] = {
    Predef.ensuring(path != null, "null is illegal")
    if (path.name.targetType==ContentType.unknown) Seq()
    else {
      // all extensions but the one requested
      val allexts = ContentType.all.filter(_ != path.name.targetType).flatMap(_.extensions)

      // requested extensions
      val reqexts = path.name.targetType.extensions

      //lookup first sources with requested extensions
      val rsources = reqexts.map(ext => lookup(path.withExt(ext))).collect({case Some(cc:ContentResource) => cc}).toSeq
      val others = mutable.ListBuffer[ContentResource]()
      if (rsources.isEmpty) {
        others ++= allexts.map(ext => lookup(path.withExt(ext))).collect({case Some(cc:ContentResource) => cc})
      }
      rsources ++ others.toSeq
    }
  }

  def createResource(path: Path, ext: Option[String]): Resource = {
    val p = if (ext.isDefined) path.withExt(ext.get) else path
    createResource(p)
  }

  def mountManager = this

  def engineManager = this

  def rootContainer = this

}


