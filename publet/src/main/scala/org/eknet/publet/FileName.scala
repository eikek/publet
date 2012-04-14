package org.eknet.publet

import resource.ContentType
import org.eknet.publet.impl.Conversions._

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 30.03.12 21:38
 */
class FileName(val path: Path) {

  Predef.ensuring(path != null, "null filenames are illegal")

  def this(path: String) = this(Path(path))
  
  private val extRegex = """\.([a-zA-Z0-9]+)$""".r

  /**
   * The content type as extracted from the extension of the file. If
   * not available, $None is returned.
   *
   */
  lazy val targetType = extension.map(ContentType(_))

  lazy val hasExtension = extRegex.findFirstMatchIn(path.asString).isDefined

  /**
   * The extension of the file of this uri, or $None if not available
   *
   */
  lazy val extension = extRegex.findFirstIn(path.segments.last).map(_.substring(1))

  /**
   * Returns a set of uris conforming to the target type of this uri. For example,
   * if this is an uri to a file `text.hmlt`, this would return two uris: `text.html`
   * and `test.htm`.
   *
   */
  lazy val pathsForTarget = pathsFor(targetType.getOrElse(throwException("Target type cannot be determined: " + path)))


  lazy val name = {
    val ext = extRegex.findFirstIn(path.asString)
    if (ext.isDefined) {
      path.segments.last.substring(0, path.segments.last.length() - ext.get.length())
    } else {
      path.segments.last
    }
  }

  /**
   * Returns a new path with the specified extension
   *
   * @param ext
   * @return
   */
  def withExtension(ext: String) = {
    val uribase = if (hasExtension)
      extRegex.replaceAllIn(path.asString, "." + ext)
    else
      path.asString + "." + ext

    Path(uribase)
  }

  def pathsFor(ct: ContentType): Set[Path] = ct.extensions.map(withExtension)

}
