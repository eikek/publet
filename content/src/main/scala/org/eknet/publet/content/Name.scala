package org.eknet.publet.content

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 01.05.13 20:11
 */
final case class Name(base: String, ext: String) {

  val fullName = base + (if (!ext.isEmpty) "." + ext else "")

  override val toString = fullName

  def hasExtension = !ext.isEmpty
  def contentType = ContentType.findByExt(ext)
  def withExtension(ext: String) = new Name(base, ext)
  def matchesType(ct: ContentType) = contentType match {
    case None => ct == ContentType.unknown
    case Some(mct) => mct == ct
  }
}

object Name {

  val empty = Name("", "")

  private val nameRegex = """^(.*)(\..*)$""".r

  implicit def apply(fullName: String): Name = fullName.trim match {
    case nameRegex(b, e) => Name(b, e.substring(1))
    case name@_ => Name(name, "")
  }

}
