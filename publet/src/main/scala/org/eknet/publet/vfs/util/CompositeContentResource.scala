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

import java.io.OutputStream
import org.eknet.publet.vfs.{ContentResource, Content, Resource}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 27.04.12 19:57
 */
class CompositeContentResource(resource: Resource, content: Content) extends ContentResource {

  def exists = resource.exists

  override def name = resource.name


  // Content interface
  def contentType = content.contentType
  def inputStream = content.inputStream
  override def lastModification = content.lastModification
  override def length = content.length

  override def copyTo(out: OutputStream) {
    content.copyTo(out)
  }

  override def contentAsString = content.contentAsString
}
