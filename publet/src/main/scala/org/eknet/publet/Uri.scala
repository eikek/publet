package org.eknet.publet

import java.net.URI

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 12:24
 */
case class Uri(uri: URI) {
  Predef.ensuring(uri != null, "null uris are illegal")

  private val extRegex = """\.([a-zA-Z0-9]+)$""".r


  lazy val schemeSymbol = Symbol(Option(uri.getScheme)
    .getOrElse(sys.error("URI is not absolute! Scheme is missing")))

  /**
   * The path of the uri, if not available an exception is thrown.
   *
   */
  lazy val path: String = Option(uri.getPath).filterNot(_.isEmpty)
    .getOrElse(sys.error("No path in URI: "+ uri))

  /**
   * The content type as extracted from the extension of the file. If
   * not available, $None is returned.
   *
   */
  lazy val targetType = extension.map(ContentType(_))

  lazy val hasExtension = extRegex.findFirstMatchIn(path).isDefined

  /**
   * The extension of the file of this uri, or $None if not available
   *
   */
  lazy val extension = extRegex.findFirstIn(path).map(_.substring(1))

  /**
   * Returns a set of uris conforming to the target type of this uri. For example,
   * if this is an uri to a file `text.hmlt`, this would return two uris: `text.html`
   * and `test.htm`.
   *
   */
  lazy val urisForTarget = urisFor(targetType.getOrElse(sys.error("Target type cannot be determined: "+ uri)))

  private def stringToTuple(str: String): (String, String) = {
    val parts = str.split('=')
    if (parts.length == 1) (parts(0), "") else (parts(0), parts(1))
  }
  lazy val parameterMap: Map[String, String] = Option(uri.getQuery).map(_.split('&').map(stringToTuple)).getOrElse(Array()).toMap

  /**
   * Returns a new URI with the specified extension
   * @param ext
   * @return
   */
  def withExtension(ext: String) = {
    val uribase = if (hasExtension)
      extRegex.replaceAllIn(asString, "."+ext)
    else
      asString +"."+ ext
    
    Uri(uribase + Option(uri.getQuery).map("?"+_).getOrElse(""))
  }

  def urisFor(ct: ContentType): Set[Uri] = ct.extensions.map(withExtension)

  private lazy val asString = schemeSymbol.name + ":///" + path

  def parameter(name: String) = parameterMap.get(name)
}

object Uri {
  
  def apply(u: String):Uri = new Uri(URI.create(u))

}
