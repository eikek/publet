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


/**
 * A modifyable resource
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.04.12 18:25
 */
trait Modifyable {
  this: Resource =>

  /**
   * Deletes this resource. If this is a container
   * it must be empty before deleting it.
   *
   */
  def delete()

  /** Creates this resource, if it does not exist, Does nothing
   * if it already exists.
   *
   * The parent container must exist.
   *
   */
  def create()

}