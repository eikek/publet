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

import scala.Some

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 25.04.12 07:23
 *
 */
case class ResourceName(name: String, ext: String) extends Ordered[ResourceName] {

  /**
   * The content type as extracted from the extension of the file.
   *
   */
  lazy val targetType = ContentType(ext)

  lazy val hasExtension = !ext.isEmpty

  lazy val fullName = name + (if (hasExtension) "."+ext else "")

  /**
   * Returns this name prefixed with a '_' character.
   */
  lazy val invisibleName = ResourceName(if (name.startsWith("_")) fullName else "_"+ fullName)

  /**
   * Returns a new name with the specified extension
   *
   * @param ext
   * @return
   */
  def withExtension(ext: String) = ResourceName(name, ext)

  def withExtIfEmpty(ext: String) = if (hasExtension) this else withExtension(ext)

  def compare(that: ResourceName) = fullName.compare(that.fullName)

  override lazy val toString = fullName

}

object ResourceName {
  private val extRegex = """\.([a-zA-Z0-9]+)$""".r

  def apply(fullname: String): ResourceName = {
    val extension = extRegex.findFirstIn(fullname).map(_.substring(1))
    val baseName = extension match {
      case Some(ext) => fullname.substring(0, fullname.length - ext.length -1)
      case None => fullname
    }
    ResourceName(baseName, extension.getOrElse(""))
  }

  class ResourceNameString(val self: String) extends Proxy {
    def rn = ResourceName(self)
  }
  implicit def string2ResourceName(str: String) = new ResourceNameString(str)
}
