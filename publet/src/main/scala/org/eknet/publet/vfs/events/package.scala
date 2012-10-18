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

import org.eknet.publet.event.Event

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 16.10.12 22:40
 */
package object events {

  /**
   * Event posted when a new container has been created.
   * @param resource
   */
  case class ContainerCreatedEvent(resource: ContainerResource) extends Event

  /**
   * Event posted when a container has been deleted.
   * @param resource
   */
  case class ContainerDeletedEvent(resource: ContainerResource) extends Event

  /**
   * Event posted when a new content file has been created (still empty).
   *
   * @param resource
   */
  case class ContentCreatedEvent(resource: ContentResource) extends Event

  /**
   * Event posted when a content file has been deleted.
   *
   * @param resource
   */
  case class ContentDeletedEvent(resource: ContentResource) extends Event

  /**
   * Event posted when a content file has been updated.
   * @param resource
   * @param changeInfo
   */
  case class ContentWrittenEvent(resource: ContentResource, changeInfo: Option[ChangeInfo]) extends Event
}
