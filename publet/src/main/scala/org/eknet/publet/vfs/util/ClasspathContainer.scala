package org.eknet.publet.vfs.util

import java.net.URL
import org.eknet.publet.vfs._

/**Serves resources relative to the the given class. Traversing
 * is not supported, only lookups.
 *
 * If path is not specified, the uri of the class resource is used.
 *
 * @param relative a path relative to the class path of `scope`. If this
 *                 is specified, a lookup of `x/y.txt` will be translated
 *                 to `relative/x/y.txt` and then looked up from the `scope`
 *                 class
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 24.04.12 23:52
 */
class ClasspathContainer(scope: Class[_],
                         relative: Option[Path]) extends Container {

  def children = List() //not working

  /**
   * Does the lookup using the specified string.
   *
   * The string is appended to `relative`, if specified.
   *
   * @param name
   * @return
   */
  def getUrl(name: String): Option[URL] = {
    val uri = relative.map(_ / name).getOrElse(Path(name).toRelative)
    Option(scope.getResource(uri.asString))
  }

  import ResourceName._

  def content(name: String) = child(name).getOrElse(Resource.emptyContent(name.rn))

  def container(name: String) = Resource.emptyContainer(name.rn)

  def child(name: String) = getUrl(name).map(toUrlResource(_, name))

  override def lookup(path: Path) = getUrl(path.asString).map(toUrlResource(_, path.name.fullName))

  private def toUrlResource(url: URL, name: String) = new UrlResource(Some(url), name.rn)

  def exists = true

  lazy val isWriteable = false
  lazy val lastModification = None
}
