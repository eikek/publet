package org.eknet.publet.vfs.util

import java.net.URL
import org.eknet.publet.vfs._

/**Serves resources relative to the the given class. Traversing
 * is not supported, only lookups.
 *
 * If path is not specified, the uri of the class resource is used.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 24.04.12 23:52
 */
class ClasspathContainer(cl: ClassLoader = Thread.currentThread().getContextClassLoader, base: String = "") extends Container {

  private val basePath = Path(base).toAbsolute

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
    val uri = basePath / name
    Option(cl.getResource(uri.toRelative.asString))
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
