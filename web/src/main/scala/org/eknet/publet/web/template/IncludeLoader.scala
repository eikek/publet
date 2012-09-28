/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.publet.web.template

import org.eknet.publet.Publet
import org.eknet.publet.vfs._
import xml.{XML, NodeSeq}
import scala.Some
import org.eknet.publet.web.{PubletWeb, PubletWebContext, Config}


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 21:12
 */
class IncludeLoader {

  val allIncludesPath = Path(Config.mainMount + "/" + Publet.allIncludes)
  val emptyResource = "/publet/templates/empty.ssp"

  def findInclude(name: String, mainInclude: Boolean = true): Option[Path] = {
    val currentPath = PubletWebContext.applicationPath
    val path = currentPath.parent
    val resource = ResourceName(name).withExtIfEmpty("html").fullName
    (findInclude(path, resource)
      orElse findAllInclude(path, resource)
      orElse {
      if (mainInclude) findMainAllInclude(resource) else None
    })
  }

  def findIncludeExtension(name: String, mainInclude: Boolean = true): Option[String] = {
    findInclude(name, mainInclude).map(_.name.ext)
  }

  /**
   * Creates a set of xml tags for the html head part. The
   * root container is searched for js, css and meta resources
   *
   * @return
   */
  def loadHeadIncludes(): NodeSeq = {
    val publet = PubletWeb.publet
    val extensions = Set("js", "css", "meta")
    val currentPath = PubletWebContext.applicationPath.parent

    val mainAll = (withMountedContainer(currentPath) {
      (path, container) =>
        publet.rootContainer.lookup(path / Publet.allIncludes)
          .collect({
          case r: ContainerResource => r
        })
          .map(r => (path / Publet.allIncludes, r))
    } orElse {
      publet.rootContainer.lookup(allIncludesPath)
        .collect({
        case r: ContainerResource => r
      })
        .map(r => (allIncludesPath, r))
    }).map(t => t._2.children
      .filter(c => extensions.contains(c.name.ext))
      .map(c => t._1 / c.name.fullName)
    ).map(_.toList)

    val incl = findNextIncludes(currentPath).map(t => t._2.children
      .filter(c => extensions.contains(c.name.ext))
      .map(c => t._1 / c.name.fullName)
    ).map(_.toList)

    for (r <- (mainAll.getOrElse(List()) ::: incl.getOrElse(List()))) yield {
      if (r.name.ext == "css") {
          <link href={PubletWebContext.urlOf(r)} rel="stylesheet"/>
      } else if (r.name.ext == "js") {
          <script type="text/javascript" src={PubletWebContext.urlOf(r)}/>
      } else {
        XML.load(publet.rootContainer.lookup(r).get.asInstanceOf[ContentResource].inputStream)
      }
    }
  }

  def findNextIncludes(path: Path): Option[(Path, ContainerResource)] = {
    val publet = PubletWeb.publet
    if (path.isRoot) None
    else {
      val cand = path / Publet.includes
      publet.rootContainer.lookup(cand) match {
        case Some(r) if (r.isInstanceOf[ContainerResource]) => Some(cand, r.asInstanceOf[ContainerResource])
        case _ => findNextIncludes(path.parent)
      }
    }
  }

  def findMainAllInclude(name: String): Option[Path] = {
    val publet = PubletWeb.publet
    val cand = allIncludesPath / name
    publet.findSources(cand).toList match {
      case c :: cs => Some(cand.sibling(c.name.fullName).toAbsolute)
      case _ => None
    }
  }

  private def withMountedContainer[A](path: Path)(f: (Path, Container) => Option[A]) = {
    val publet = PubletWeb.publet
    publet.mountManager.resolveMount(path).flatMap(tuple => f(tuple._1, tuple._2))
  }

  def findAllInclude(path: Path, name: String): Option[Path] = {
    val publet = PubletWeb.publet
    withMountedContainer(path) {
      (path, container) =>
        val cand = path / Publet.allIncludes / name
        publet.findSources(cand).toList match {
          case c :: cs => Some(cand.sibling(c.name.fullName))
          case _ => None
        }
    }
  }

  def findInclude(path: Path, name: String): Option[Path] = {
    if (path.isRoot) None
    else {
      val cand = path / Publet.includes / name
      PubletWeb.publet.findSources(cand).toList match {
        case c :: cs => Some(cand.sibling(c.name.fullName))
        case _ => findInclude(path.parent, name)
      }
    }
  }

  def isResourceEditable: Boolean = PubletWeb.publet.findSources(PubletWebContext.applicationPath).toList match {
    case c :: cs => c.isInstanceOf[Writeable]
    case _ => false
  }
}
