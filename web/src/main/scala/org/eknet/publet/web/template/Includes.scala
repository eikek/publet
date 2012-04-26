package org.eknet.publet.web.template

import org.eknet.publet.Publet
import org.eknet.publet.vfs._
import xml.NodeSeq
import org.slf4j.LoggerFactory
import org.eknet.publet.web.{Config, WebContext}
import org.eknet.publet.engine.PubletEngine

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 12:32
 */
trait Includes {
  private val log = LoggerFactory.getLogger(classOf[Includes])

  val allIncludesPath = Path(Config.mainMount+"/.allIncludes").toAbsolute
  val includesString = ".includes"

  /**
   * Returns a relative path to the specified resource name at `/.allIncludes` location.
   *
   * For example, for the path `/a/b/c.html` it would return `../../.allIncludes/c.html`
   *
   * @param path
   * @return
   */
  def allIncludes(path:Path) = path.relativeRoot + allIncludesPath.name+ "/" + path.name.fullName

  /**
   * Returns a path to the specified resource name at `./.includes` location.
   *
   * For example, for the path `/a/b/c.html` it would return `/a/b/.includes/c.html`.
   *
   * @param path
   * @return
   */
  def includes(path: Path) = path.sibling(includesString) / path.name

  def publet: Publet

  def includeEngine: PubletEngine

  /**
   * Gets the content for the path by first looking at `.includes`
   * and then at `/.allIncludes`.
   *
   * The path `/a/b/c/d.html` is translated to `/a/b/.includes/d.htlm`
   * and after that to `/.allIncludes/d.html`.
   *
   * @param path
   * @return
   */
  def getInclude(path: Path): Option[Content] = {
    log.trace("get include: {} and {}", includes(path).asString, (allIncludesPath / path.name).asString)
    publet.process(includes(path), ContentType.html, includeEngine).orElse {
      publet.process(allIncludesPath/path.name, ContentType.html, includeEngine)
    }
  }

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

  /**
   * Returns a list of all resources at `/.allIncludes`
   *
   * @return
   */
  def getRootResources: List[ContentResource] = {
    publet.rootContainer.lookup(allIncludesPath) match {
      case Some(c: ContainerResource) => c.children.collect({ case p: ContentResource => p }).toList
      case _ => List()
    }
  }

  /**
   * Returns a list of all resources at `./.includes` (relative to the
   * specified path).
   *
   * @param path
   * @return
   */
  def getSiblingResources(path: Path): List[ContentResource] = {
    val incl = path.sibling(includes(path).asString)
    publet.rootContainer.lookup(incl) match {
      case Some(c: ContainerResource) => c.children.collect({ case p:ContentResource => p}).toList
      case _ => List()
    }
  }
}
