package org.eknet.publet.engine

import org.eknet.publet.{ContentType, Data}


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 12:46
 */
class DefaultConverterEngine extends ConverterEngine with ConverterRegistry {

  def name = 'convert

  def process(data: Seq[Data], target: ContentType) = {
    process(data.head, target).fold(Left(_), _ match {
      case None => process(data.tail, target)
      case Some(x) => Right(x)
    })
  }
  
  private def process(data: Data, target: ContentType): Either[Exception, Option[Data]] = {
    try {
      converterFor(data.contentType, target) match {
        case None => Right(None)
        case Some(c) => Right(Option(c(data)))
      }
    } catch {
      case e: Exception => Left[Exception, Option[Data]](e)
    }
  }
}
