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

package org.eknet.publet.vfs.util

import java.net.URL
import org.eknet.publet.vfs._

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.04.12 23:14
 */
class UrlResource(val url: Option[URL], val name: ResourceName) extends ContentResource {

  def this(url: URL, name: ResourceName) = this(Some(url), name)

  def this(url: URL) = this(Some(url), ResourceName(url.getFile))

  override def lastModification = url.get.openConnection().getLastModified match {
    case 0 => None
    case x => Some(x)
  }

  def isWriteable = try {
    if (url.isDefined) {
      url.get.openConnection().getOutputStream
      true
    } else {
      false
    }
  } catch {
    case _ => false
  }

  def exists = url.isDefined

  def inputStream = url.get.openStream()

  override def length = url.flatMap(u => u.openConnection().getContentLength match {
    case -1 => None
    case x => Some(x)
  })

  def contentType = name.targetType

  override def toString = "Url[" + url + "]"
}
