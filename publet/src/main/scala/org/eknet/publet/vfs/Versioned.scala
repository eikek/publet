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

/** A versioned resource.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 06.05.12 01:53
 */
trait Versioned {
  this: Resource =>

  /**
   * Returns current version
   * of this resource.
   *
   * @return
   */
  def currentVersion: Version


  case class Version(id: String)
}
