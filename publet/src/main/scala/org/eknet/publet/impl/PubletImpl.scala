package org.eknet.publet.impl

import scala.collection.mutable
import org.eknet.publet.impl.Conversions._
import org.eknet.publet._
import engine.{PubletEngine, EngineMangager}
import vfs._

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

  def push(path: Path, content: Content) {
    findSources(path) match {
      case Nil => create(path, content.contentType); push(path, content)
      case c => c.head.writeFrom(content.inputStream)
    }
  }

  def findSources(path: Path): Iterable[ContentResource] = {
    Predef.ensuring(path != null, "null is illegal")

    def matchType(r:Resource):Boolean = {
      r match {
        case cc: ContentResource if (cc.contentType == path.name.targetType) => true
        case _ => false
      }
    }
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

  def create(path: Path, contentType: ContentType) {
    val t = resolveMount(path).getOrElse(throwException("No partition mounted for path: "+ path))
    val p = path.strip(t._1)
    t._2 match {
      case mc: ModifyableContainer => mc.createContent(p.withExt(contentType.extensions.head))
      case _ => sys.error("Unmodifyable resource")
    }
  }

  def mountManager = this

  def engineManager = this

  def rootContainer = this

}


