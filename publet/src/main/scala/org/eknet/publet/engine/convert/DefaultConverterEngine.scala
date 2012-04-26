package org.eknet.publet.engine.convert

import org.eknet.publet.vfs.{Path, ContentType, Content}
import org.eknet.publet.engine.PubletEngine

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 12:46
 */
class DefaultConverterEngine(val name: Symbol) extends PubletEngine with ConverterEngine with ConverterRegistry {

  def this() = this('convert)

  def process(path: Path, data: Seq[Content], target: ContentType): Content = {
    //if target type is available return it, otherwise try to process
    data.find(_.contentType == target) match {
      case Some(c) => c
      case None => process(path,  data.head, target) match {
        case None => data.tail match {
          case Nil => sys.error("no converter found: "+ data.head.contentType+" -> "+ target)
          case tail => process(path, tail, target)
        }
        case Some(x) => x
      }
    }
  }

  private def process(path: Path, data: Content, target: ContentType): Option[Content] = {
    converterFor(data.contentType, target) match {
      case None => None
      case Some(c) => Option(c(path, data))
    }
  }
}
