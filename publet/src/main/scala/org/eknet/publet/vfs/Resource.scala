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

import java.io.{File, ByteArrayInputStream}
import org.eknet.publet.vfs.util.{ClasspathResource, UrlResource}
import org.eknet.publet.vfs.fs.FileResource
import java.net.URL


/**
 * A resource is an abstract named content, like a file on a
 * local or remote file system. It may also be a directory or
 * any other container like resource.
 *
 * This resource abstraction may point to an non-existing
 * resource.
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 13:59
 */
trait Resource {

  /**
   * If available, returns the alst modification timestamp
   * of this resource.
   *
   * @return
   */
  def lastModification: Option[Long]

  /**
   * The name of this resource.
   *
   * @return
   */
  def name: ResourceName

  /**
   * Tells, whether this resource exists.
   *
   * @return
   */
  def exists: Boolean

  /**
   * Applies the specified function to this resource, if `exists`
   * returns `true`. Otherwise returns `None`
   *
   * @param f
   * @tparam A
   * @return
   */
  def map[A](f:Resource.this.type=>Option[A]):Option[A] = {
    if (exists) f(this)
    else None
  }

}

trait ContentResource extends Resource with Content
trait ContainerResource extends Resource with Container

object Resource {

  val resourceComparator = (r1: Resource, r2: Resource) => {
    if (isContainer(r1) && !isContainer(r2)) true
    else if (isContainer(r2) && !isContainer(r1)) false
    else r1.name.compareTo(r2.name) < 0
  }

  def classpath(uri: String, loader: Option[ClassLoader] = None, name: Option[ResourceName] = None): ContentResource = {
    new ClasspathResource(uri, loader, name)
  }

  def file(path: String): ContentResource = new UrlResource(new File(path).toURI.toURL)

  def isContainer(r:Resource):Boolean = r match {
    case r:Container => true
    case _ => false
  }

  def isContent(r:Resource): Boolean = r match {
    case r:Content => true
    case _ => false
  }

  def toModifyable(r: Resource): Option[Modifyable] = {
    r match {
      case m:Modifyable=> Some(m)
      case _ => None
    }
  }

  def emptyContainer(name: ResourceName):ContainerResource = new EmptyContainer(name)
  def emptyContent(name: ResourceName, ct: ContentType = ContentType.unknown): ContentResource = new EmptyContent(name, ct)

  private class EmptyContainer(val name: ResourceName) extends ContainerResource {
    import ResourceName._

    def exists = false
    def children = List()
    def content(name: String) = emptyContent(name.rn)
    def container(name: String) = emptyContainer(name.rn)
    def child(name: String) = None
    def lastModification = None
    lazy val isWriteable = false
  }

  private class EmptyContent(val name: ResourceName, val contentType: ContentType) extends ContentResource {

    def exists = false
    def inputStream = new ByteArrayInputStream(Array[Byte]())
  }

}