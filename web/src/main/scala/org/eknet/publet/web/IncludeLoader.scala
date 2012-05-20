package org.eknet.publet.web

import org.eknet.publet.vfs.Path
import org.eknet.publet.Publet


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 21:12
 */
class IncludeLoader {

  val allIncludesPath = Path(Config.mainMount + "/"+ Publet.allIncludes)
  val emptyResource = "/publet/templates/empty.ssp"

  def loadInclude(name: String): String = {
    val currentPath = PubletWebContext.applicationPath
    findInclude(currentPath.parent, name+".html").map(_.asString) getOrElse emptyResource
  }

  private def findInclude(path: Path, name: String): Option[Path] = {
    if (path.isRoot) None
    else {
      val cand = path / ".includes" / name
      PubletWeb.publet.findSources(cand).toList match {
        case c::cs => Some(cand.sibling(c.name.fullName))
        case _ => findInclude(path.parent, name)
      }
    }
  }
}
