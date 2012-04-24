package org.eknet.publet.web.template

import org.eknet.publet.Path
import org.eknet.publet.web.WebContext
import xml.NodeSeq
import org.eknet.publet.resource._

/** Includes all resources from `/.allIncludes` in every page. Includes all resources
 * from `./.includes` into the current page.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 14.04.12 15:57
 */
trait IncludesTemplate extends HtmlTemplate {
  
  override def headerHtml(path: Path, content: Content, source: Seq[Content]) = {
    val siblingPath = (cr: ContentResource) => ".includes/"+cr.path.segments.last
    val rootPath = (cr: ContentResource) => path.relativeRoot + ".allIncludes/"+cr.path.segments.last

    super.headerHtml(path, content, source) +
      getRootResources.map(include(_, rootPath)).foldLeft(NodeSeq.Empty)((a,b) => a++b) +
      getSiblingResources(path).map(include(_, siblingPath)).foldLeft(NodeSeq.Empty)((a,b) => a++b)
  }
  
  private def include(cr: ContentResource, f:ContentResource=>String): NodeSeq = {
    val crp = f(cr)
    cr.contentType match {
      case ContentType.javascript => <script src={ crp }></script>
      case ContentType.css => <link rel="stylesheet" href={ crp } ></link>
      case _ => NodeSeq.Empty
    }
  }
  
  private def getRootResources: List[ContentResource] = {
    val publet = WebContext().publet
    publet.lookup(Path("/.allIncludes/")) match {
      case Some(c: ContainerResource) => c.children.collect({ case p: ContentResource => p }).toList
      case _ => List()
    }
  }
  
  private def getSiblingResources(path: Path): List[ContentResource] = {
    val incl = path.sibling(".includes/")
    val publet = WebContext().publet
    publet.lookup(incl) match {
      case Some(c: ContainerResource) => c.children.collect({ case p:ContentResource => p}).toList
      case _ => List()
    }
  }
}
