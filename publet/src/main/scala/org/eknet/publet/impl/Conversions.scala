package org.eknet.publet.impl

import java.net.URI
import org.eknet.publet.Data

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 23:10
 */
protected[publet] object Conversions {

  implicit def toSymbolUri(uri:URI) = new SymbolUri(uri)

  class SymbolUri(uri: URI) {

    def schemeSymbol = uri.getScheme match {
      case null => sys.error("URI is not absolute! Scheme is missing")
      case s => Symbol(s)
    }

    def validPath: String = Option(uri.getPath).filterNot(_.isEmpty).get

  }
  
  implicit def toOptionalEither(v: Either[Exception, Data]): Either[Exception, Option[Data]] = {
    v match {
      case Right(data) => Right(Option(data))
      case Left(x) => Left(x)
    }
  }
  
}
