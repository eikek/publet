package org.eknet.publet.engine

import org.eknet.publet.{ContentType, Page}


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 12:46
 */
class DefaultConverterEngine extends ConverterEngine with ConverterRegistry {

  def name = 'convert

  def process(data: Seq[Page], target: ContentType) = {
    process(data.head, target).fold(Left(_), _ match {
      case None => process(data.tail, target)
      case Some(x) => Right(x)
    })
  }
  
  private def process(data: Page, target: ContentType): Either[Exception, Option[Page]] = {
    try {
      converterFor(data.contentType, target) match {
        case None => Right(None)
        case Some(c) => Right(Option(c(data)))
      }
    } catch {
      case e: Exception => Left[Exception, Option[Page]](e)
    }
  }
}
