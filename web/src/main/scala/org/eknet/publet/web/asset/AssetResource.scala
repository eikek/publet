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

package org.eknet.publet.web.asset

import org.eknet.publet.vfs.ContentResource
import org.eknet.publet.vfs.util.ForwardingContentResource

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.09.12 17:06
 */
final class AssetResource(val delegate: ContentResource,
                          val target: Option[String] = None,
                          val compress: Boolean = true,
                          val group: String = null) extends ForwardingContentResource {

  private[asset] def inGroup(name: String) = new AssetResource(delegate, target, compress, name)

  /**
   * Specifies that this resource should not be compressed.
   *
   * @return
   */
  def noCompress = new AssetResource(delegate, target, compress = false)

  /**
   * Specifies a target directory where the resource is mounted. There exists
   * the following defaults:
   *
   * * for `js` files, the folder "js"
   * * for `css` files, the folder "css"
   * * for image files, the folder "img"
   * * for other files, the folder "other"
   *
   * It might be necessary to change this, if the supplied css files reference
   * images or other resources relative to itself.
   *
   * @param target
   * @return
   */
  def into(target: String) = new AssetResource(delegate, Some(target), compress)

  override def toString = delegate.toString
}
