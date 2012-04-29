package org.eknet.publet.engine.convert

import org.eknet.publet.engine.PubletEngine
import org.eknet.publet.vfs.{Path, ContentResource, ContentType, Content}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 12:46
 */
class DefaultConverterEngine(val name: Symbol) extends PubletEngine with ConverterEngine with ConverterRegistry {

  def this() = this('convert)

  def process(path: Path, data: Seq[ContentResource], target: ContentType): Option[Content] = {
    //if target type is available return it, otherwise try to process
    data.find(_.contentType == target) match {
      case a@Some(c) => a
      case None => process(path, data.head, target) match {
        case None => data.tail match {
          case Nil => sys.error("no converter found: "+ data.head.contentType+" -> "+ target)
          case tail => process(path, tail, target)
        }
        case a@Some(x) => a
      }
    }
  }

  private def process(path: Path, data: ContentResource, target: ContentType): Option[Content] = {
    converterFor(data.contentType, target) match {
      case None => None
      case Some(c) => Option(c(path, data))
    }
  }
}
