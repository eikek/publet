package org.eknet.publet.engine.convert

import org.eknet.publet.vfs.ContentType._
import org.eknet.publet.vfs.{Path, Content}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 12.04.12 14:13
 */

class CodeHtmlConverter(langClass: Option[String]) extends ConverterEngine#Converter {

  def apply(path: Path, v1: Content) = {
    val body = v1.contentAsString.replaceAll("&amp;", "&").replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;")
    val code = langClass match {
      case None => "<code>"+ body +"</code>"
      case Some(c) => "<code class=\""+c+"\">"+ body +"</code>"
    }

    Content("<pre>"+ code +"</pre>", html)
  }
}

object CodeHtmlConverter {

  def apply(): CodeHtmlConverter = new CodeHtmlConverter(None)

  def scala: CodeHtmlConverter = new CodeHtmlConverter(Some("scala"))

  def json: CodeHtmlConverter = new CodeHtmlConverter(Some("json"))

  def css: CodeHtmlConverter = new CodeHtmlConverter(Some("css"))
}
