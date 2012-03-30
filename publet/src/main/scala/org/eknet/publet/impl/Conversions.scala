package org.eknet.publet.impl

import org.eknet.publet.Page

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 23:10
 */
protected[publet] object Conversions {


  implicit def toOptionalEither(v: Either[Exception, Page]): Either[Exception, Option[Page]] = {
    v match {
      case Right(data) => Right(Option(data))
      case Left(x) => Left(x)
    }
  }
}
