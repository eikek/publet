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
    val siblingPath = (p:Path, cr: ContentResource) => includes(path).asString
    val rootPath = (p:Path, cr: ContentResource) => allIncludes(path)

    val h = getRootResources.map(include(_, rootPath)).foldLeft(NodeSeq.Empty)((a,b) => a++b)
    getSiblingResources(path).map(include(_, siblingPath)).foldLeft(NodeSeq.Empty)((a,b) => a++b)
    h.toString()
  }

  private def include(in:(Path, ContentResource), f:(Path, ContentResource)=>String): NodeSeq = {
    val crp = f(in._1, in._2)
    in._2.contentType match {
      case ContentType.javascript => <script src={ crp }></script>
      case ContentType.css => <link rel="stylesheet" href={ crp } ></link>
      case _ => NodeSeq.Empty
    }
  }

}
