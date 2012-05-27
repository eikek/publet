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

package org.eknet.publet.vfs

/** A container for resources.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 05.04.12 11:56
 *
 */
trait Container {

  /**
   * The child list of this container.
   *
   * @return
   */
  def children: Iterable[_ <: Resource]

  /**
   * Returns a content resource relative to this
   * container. The resource may not exist.
   *
   * @param name
   * @return
   */
  def content(name: String): ContentResource


  /**
   * Returns a container resource relative to this. The
   * resource may exist or not.
   *
   * @param name
   * @return
   */
  def container(name: String): ContainerResource

  /**
   * Returns an child
   *
   * @param name
   * @return
   */
  def child(name: String): Option[Resource]

  /**
   * Looks up a resource at the specified path relative
   * to this container.
   *
   * The default implementation will look for an existing
   * child using the first segment of the path and recursively
   * calls lookup on that child.
   *
   * @param path
   * @return
   */
  def lookup(path: Path): Option[Resource] = {
    path.segments match {
      case a :: Nil => child(a)
      case a :: _ => container(a).map(_.lookup(path.strip))
      case Nil => None
    }
  }

  def foreach(path: Path, f:(Path, Resource)=>Unit, maxDepth:Int = -1) {
    def eachChild(path: Path, cr: ContainerResource, curDepth: Int) {
      if (maxDepth < 0 || curDepth < maxDepth) {
        cr.children.foreach(c => {
          val p = path / c.name
          c match {
            case cont: ContainerResource => f(p, cont); eachChild(p, cont, curDepth+1)
            case a => f(p, a)
          }
        })
      }
    }
    lookup(path) match {
      case Some(r) => {
        f(path, r) //depth 0
        r match {
          case cr: ContainerResource => eachChild(path, cr, 0)
          case _ =>
        }
      }
      case None =>
    }
  }

  /**
   * Travels down the path segments in this container
   * creating any missing container item until the
   * end of the path.
   *
   * @param path
   */
  def createResource(path: Path): Resource = {
    def newResource(name:String, dir: Boolean): Resource = {
      val d = if (dir) container(name) else content(name)
      if (!d.exists) d match {
        case md:Modifyable => md.create()
        case _ => sys.error("Container '"+name+"' not modifyable. Cannot create resource at: "+ path.asString)
      }
      d
    }
    path.segments match {
      case a :: Nil => newResource(a, path.directory)
      case a :: as => newResource(a, true).asInstanceOf[ContainerResource].createResource(path.tail)
      case Nil => sys.error("Cannot create new resource at root!")
    }
  }

  /**Wether this container is able to create new
   * child resources.
   *
   * @return
   */
  def isWriteable: Boolean
}
