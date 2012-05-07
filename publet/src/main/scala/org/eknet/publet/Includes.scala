package org.eknet.publet

import engine.PubletEngine
import vfs._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 19:05
 */
trait Includes {

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

  def publet: Publet

  /**
   * The engine used to process the resources in [[.processInclude]]
   * @return
   */
  def includeEngine: PubletEngine

  /**
   * Processes the path by first looking at `.includes` and travelling
   * up the hierarchy. If that fails, it then tries at last `/.allIncludes`.
   *
   * The path `/a/b/c/d.html` is translated to `/a/b/c/.includes/d.html`
   * and after that to `/.allIncludes/d.html`.
   *
   * @param path
   * @return
   */
  def processInclude(path: Path): Option[Content] = {
    def recursiveInclude(p: Path): Option[Content] = {
      val cand = p.sibling(Includes.includes) / p.name
      publet.process(cand, ContentType.html, includeEngine) orElse {
        if (p.size > 1) recursiveInclude(p.parent.sibling(p.name.fullName))
        else None
      }
    }
    recursiveInclude(path).orElse {
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
   * Returns a list of all resources at `./.includes`. It first tries at
   * `.includes/` relative to the given path and travels up the hierarchy
   * until a `.includes/` folder is found.
   *
   * @param path
   * @return
   */
  def getIncludesResources(path: Path): List[(Path, ContentResource)] = {
    val incl = path.sibling(Includes.includes)
    publet.rootContainer.lookup(incl) match {
      case Some(c: ContainerResource) => c.children.collect({ case p:ContentResource => p}).map(c=>(incl/c.name, c)).toList
      case _ => if (path.size>0) getIncludesResources(path.parent) else List()
    }
  }
}

object Includes {
  /**
   * The string `.allIncludes/` constant
   *
   */
  val allIncludes = ".allIncludes/"
  val allIncludesPath = Path(allIncludes)

  val includes = ".includes/"
  val includesPath = Path(includes)
}
