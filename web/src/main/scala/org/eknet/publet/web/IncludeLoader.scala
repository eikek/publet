package org.eknet.publet.web

import org.eknet.publet.Publet
import org.eknet.publet.vfs.{Writeable, ResourceName, Path}


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 21:12
 */
class IncludeLoader {

  val allIncludesPath = Path(Config.mainMount + "/"+ Publet.allIncludes)
  val emptyResource = "/publet/templates/empty.ssp"

  /**Returns the path to the resource of the given name
   * in one of the include folders or the `emptyResource`
   * @param name
   * @return
   */
  def loadInclude(name: String): String = {
     findInclude(name) getOrElse emptyResource
  }

  def findInclude(name: String): Option[String] = {
    val currentPath = PubletWebContext.applicationPath
    val path = currentPath.parent
    val resource = ResourceName(name).withExtIfEmpty("html").fullName
    (findInclude(path, resource)
      orElse findAllInclude(path, resource)
      orElse findMainAllInclude(resource)).map(_.asString)
  }

  def findMainAllInclude(name: String): Option[Path] = {
    val publet = PubletWeb.publet
    val cand = allIncludesPath / name
    publet.findSources(cand).toList match {
      case c::cs => Some(cand.sibling(c.name.fullName))
      case _ => None
    }
  }

  def findAllInclude(path: Path, name: String): Option[Path] = {
    val publet = PubletWeb.publet
    publet.mountManager.resolveMount(path).flatMap(tuple => {
      val cand = tuple._1 / Publet.allIncludes / name
      publet.findSources(cand).toList match {
        case c::cs => Some(cand.sibling(c.name.fullName))
        case _ => None
      }
    })
  }

  def findInclude(path: Path, name: String): Option[Path] = {
    if (path.isRoot) None
    else {
      val cand = path / ".includes" / name
      PubletWeb.publet.findSources(cand).toList match {
        case c::cs => Some(cand.sibling(c.name.fullName))
        case _ => findInclude(path.parent, name)
      }
    }
  }

  def isResourceEditable:Boolean = PubletWeb.publet.findSources(PubletWebContext.applicationPath).toList match {
    case c::cs => c.isInstanceOf[Writeable]
    case _ => false
  }
}
