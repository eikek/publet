package org.eknet.publet

import engine.PubletEngine
import vfs._
import org.slf4j.LoggerFactory

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 19:05
 */
trait Includes {
  private val log = LoggerFactory.getLogger(getClass)

  def allIncludesPath = Path(pathPrefix+ "/"+ Includes.allIncludes).toAbsolute

  def pathPrefix = ""

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
  def includes(path: Path) = path.sibling(Includes.includes) / path.name

  def publet: Publet

  def includeEngine: PubletEngine

  /**
   * Processes the path by first looking at `.includes`
   * and then at `/.allIncludes`.
   *
   * The path `/a/b/c/d.html` is translated to `/a/b/.includes/d.html`
   * and after that to `/.allIncludes/d.html`.
   *
   * @param path
   * @return
   */
  def processInclude(path: Path): Option[Content] = {
    log.trace("get include: {} and {}", includes(path).asString, (allIncludesPath / path.name).asString)
    publet.process(includes(path), ContentType.html, includeEngine).orElse {
      publet.process(allIncludesPath/path.name, ContentType.html, includeEngine)
    }
  }


  /**
   * Returns a list of all resources at `/.allIncludes`
   *
   * @return
   */
  def getRootResources: List[(Path, ContentResource)] = {
    publet.rootContainer.lookup(allIncludesPath) match {
      case Some(c: ContainerResource) => c.children.collect({ case p: ContentResource => p }).map(c=>(allIncludesPath/c.name, c)).toList
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
  def getSiblingResources(path: Path): List[(Path, ContentResource)] = {
    val incl = path.sibling(includes(path).asString)
    publet.rootContainer.lookup(incl) match {
      case Some(c: ContainerResource) => c.children.collect({ case p:ContentResource => p}).map(c=>(incl/c.name, c)).toList
      case _ => List()
    }
  }
}

object Includes {
  val allIncludes = ".allIncludes/"
  val allIncludesPath = Path(allIncludes)

  val includes = ".includes/"
  val includesPath = Path(includes)
}
