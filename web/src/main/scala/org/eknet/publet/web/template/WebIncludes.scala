package org.eknet.publet.web.template

import org.eknet.publet.vfs._
import xml.NodeSeq
import org.eknet.publet.Includes
import org.eknet.publet.web.Config

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 12:32
 */
trait WebIncludes extends Includes {

  override def pathPrefix = Config.mainMount

  /**
   * Returns a string for all html header includes.
   *
   * It scans the directory `/.allIncludes` and the `./.includes` (relative
   * to the specified path) for css and js files and creates html script
   * and link tags.
   *
   * @param path
   * @return
   */
  def headerIncludes(path: Path) = {
    val siblingPath = (cr: ContentResource) => includes(cr.path).asString
    val rootPath = (cr: ContentResource) => allIncludes(cr.path)

    val h = getRootResources.map(include(_, rootPath)).foldLeft(NodeSeq.Empty)((a,b) => a++b)
    getSiblingResources(path).map(include(_, siblingPath)).foldLeft(NodeSeq.Empty)((a,b) => a++b)
    h.toString()
  }

  private def include(cr: ContentResource, f:ContentResource=>String): NodeSeq = {
    val crp = f(cr)
    cr.contentType match {
      case ContentType.javascript => <script src={ crp }></script>
      case ContentType.css => <link rel="stylesheet" href={ crp } ></link>
      case _ => NodeSeq.Empty
    }
  }

}
