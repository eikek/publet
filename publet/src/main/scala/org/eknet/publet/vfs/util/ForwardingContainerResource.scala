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

import org.eknet.publet.vfs.ContainerResource

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.04.12 19:26
 */
trait ForwardingContainerResource extends ForwadingResource with ContainerResource {

  protected def delegate: ContainerResource

  def children = delegate.children

  def content(name: String) = delegate.content(name)

  def container(name: String) = delegate.container(name)

  def child(name: String) = delegate.child(name)
}
