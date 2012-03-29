package org.eknet.publet.impl

import org.eknet.publet.Data

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 23:10
 */
protected[publet] object Conversions {


  implicit def toOptionalEither(v: Either[Exception, Data]): Either[Exception, Option[Data]] = {
    v match {
      case Right(data) => Right(Option(data))
      case Left(x) => Left(x)
    }
  }
}
