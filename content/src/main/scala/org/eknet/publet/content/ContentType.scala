package org.eknet.publet.content

import java.util.concurrent.atomic.AtomicReference
import scala.annotation.tailrec
import java.nio.charset.Charset

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.05.13 17:34
 */
abstract sealed class ContentType {

  def mediaType: String
  def subType: String
  def extensions: Set[String]
  def binary: Boolean
  def charset: Option[Charset]

  override val toString = mediaType +"/"+ subType + charset.map(cs => "; charset="+cs.name()).getOrElse("")
}

object ContentType {

  private val types = new AtomicReference(Map.empty[(String, String), ContentType])
  private val extensionMap = new AtomicReference(Map.empty[String, ContentType])

  def unapply(contentType: ContentType): Option[(String, String, Boolean)] = Some(contentType.mediaType, contentType.subType, contentType.binary)

  def register(ct: ContentType) = {
    registerType(ct)
    registerExtension(ct)
    ct
  }

  @tailrec
  private def registerType(ct: ContentType) {
    val map = types.get()
    val next = map + ((ct.mediaType.toLowerCase -> ct.subType.toLowerCase) -> ct)
    if (!types.compareAndSet(map, next)) {
      registerType(ct)
    }
  }

  private def registerExtension(ct: ContentType) {
    @tailrec
    def registerFilext(ext: String) {
      val map = extensionMap.get
      val existing = map.get(ext)
      require(existing.isEmpty, s"Unable to register ${ct}! The extension $ext clashes with content type ${existing.get} ")
      val next = map + (ext -> ct)
      if (!extensionMap.compareAndSet(map, next)) {
        registerFilext(ext)
      }
    }

    ct.extensions.foreach(registerFilext)
  }

  def all = types.get().values

  def get(ct: (String, String)) = types.get().get(ct)

  def findByExt(extension: String): Option[ContentType] = extensionMap.get.get(extension)


  /**
   * Create a custom content type and register it using
   * {{{
   *   ContentType.register(mycontenttype)
   * }}}
   * This way it is also possible to override existing ones.
   *
   * @param mediaType
   * @param subType
   * @param extensions
   * @param binary
   * @param charset
   */
  case class CustomContentType(mediaType: String,
                               subType: String,
                               extensions: Set[String] = Set(),
                               binary: Boolean = false,
                               charset: Option[Charset] = None) extends ContentType {

    def withCharset(cs: Charset) = copy(charset = Some(cs))
    def withoutCharset = copy(charset = None)
    def withExt(ext: String) = copy(extensions = extensions + ext)
    def withoutExt(ext: String) = copy(extensions = extensions - ext)
  }

  private def create(mainType: String, subType: String, extensions: Set[String] = Set(), binary: Boolean = false, charset: Option[Charset] = None) = {
    register {
      CustomContentType(mainType, subType, extensions, binary, charset)
    }
  }


  // ~~ predefined content types

  private def text(subType: String, extensions: String*) = create("text", subType, extensions.toSet)
  private def textUtf8(subType: String, extensions: String*) = create("text", subType, extensions.toSet, false, Some(Charsets.utf8))
  private def app(subType: String, extensions: String*) = create("application", subType, extensions.toSet)
  private def appUtf8(subType: String, extensions: String*) = create("application", subType, extensions.toSet, false, Some(Charsets.utf8))
  private def appBinary(subType: String, extensions: String*) = create("application", subType, extensions.toSet, true)
  private def image(subType: String, extensions: String*) = create("image", subType, extensions.toSet, true)

  val unknown = appBinary("octet-stream")

  val `text/plain` = text("plain", "txt", "text", "properties", "cfg") //"conf" is for confluence markup
  val `text/html` = text("html", "htm", "html", "htmls", "htx", "shtml")
  val `text/x-scala` =  text("x-scala", "scala")
  val `text/x-markdown` = textUtf8("x-markdown", "md", "markdown", "mdown", "mkdn", "mdwn", "mdtxt", "mdtext")
  val `text/x-textile` = textUtf8("x-textile", "textile")
  val `text/x-confluence` = textUtf8("x-confluence", "conf")
  val `text/x-ssp` = textUtf8("x-ssp", "ssp")
  val `text/x-mutache` = textUtf8("x-mustache", "mustache")
  val `text/x-scaml` = textUtf8("x-scaml", "scaml")
  val `text/css` = text("css", "css")

  val `application/xml` = app("xml", "xml", "xsl", "xsd")
  val `application/javascript` = app("javascript", "js")
  val `application/json` = appUtf8("json", "json")

  val `image/png` = image("png", "png")
  val `image/jpeg` = image("jpeg", "jpg", "jpeg", "jpe")
  val `image/gif` = image("gif", "gif")
  val `image/x-icon` = image("x-icon", "ico")

  val `application/pdf` = appBinary("pdf", "pdf")
  val `application/zip` = appBinary("zip", "zip")
  val `application/jar` = appBinary("java-archive", "jar")

}
