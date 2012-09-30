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
import org.eknet.publet.vfs.util.UrlResource

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.09.12 17:05
 */
trait AssetCollection {
  import org.eknet.publet.vfs.Path._

  /**
   * Specify a base path from which to resolve resources
   * with `findResource` and `resource` in the current
   * classpath.
   *
   * Default is "/".
   *
   * @return
   */
  def classPathBase: String = "/"


  final def findResource(name: String) = Option(getClass.getResource(
    (classPathBase.p / name).asString))

  final def resource(name: String) = new UrlResource(findResource(name)
    .getOrElse(sys.error("Resource '"+ classPathBase+"/"+ name+"' not found.")))

  implicit def toAssetResource(r: ContentResource) = new AssetResource(r)
  implicit def toResource(ri: AssetResource) = ri.delegate
}
