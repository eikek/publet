package org.eknet.publet.engine.convert

import org.eknet.publet.resource.{ContentType, Content}
import org.eknet.publet.Path
import org.eknet.publet.engine.PubletEngine

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 12:46
 */
class DefaultConverterEngine(val name: Symbol) extends PubletEngine with ConverterEngine with ConverterRegistry {

  def this() = this('convert)

  def process(path: Path, data: Seq[Content], target: ContentType): Either[Exception, Content] = {
    //if target type is available return it, otherwise try to process
    data.find(_.contentType == target) match {
      case Some(c) => Right(c)
      case None => process(data.head, target).fold(Left(_), _ match {
        case None => data.tail match {
          case Nil => Left(new RuntimeException("no converter found"))
          case tail => process(path, tail, target)
        }
        case Some(x) => Right(x)
      })
    }
  }

  private def process(data: Content, target: ContentType): Either[Exception, Option[Content]] = {
    try {
      converterFor(data.contentType, target) match {
        case None => Right(None)
        case Some(c) => Right(Option(c(data)))
      }
    } catch {
      case e: Exception => Left[Exception, Option[Content]](e)
    }
  }
}
