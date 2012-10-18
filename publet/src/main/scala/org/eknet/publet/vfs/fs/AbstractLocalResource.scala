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

package org.eknet.publet.vfs.fs

import java.io.File
import org.eknet.publet.vfs._
import com.google.common.eventbus.EventBus
import org.eknet.publet.vfs.events.{ContentDeletedEvent, ContainerDeletedEvent}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.04.12 20:44
 */
abstract class AbstractLocalResource(val file: File, val rootPath: Path, bus: EventBus) extends Resource with FileResourceFactory {

  def name = if (file.isDirectory) ResourceName(file.getName+"/") else ResourceName(file.getName)

  def parent = if (Path(file.getAbsolutePath).size == rootPath.size) None else Some(newDirectory(file.getParentFile, rootPath, bus))

  def exists = file.exists()

  def delete() {
    file.delete()
    if (this.isInstanceOf[DirectoryResource]) {
      bus.post(ContainerDeletedEvent(this.asInstanceOf[DirectoryResource]))
    }
    if (this.isInstanceOf[FileResource]) {
      bus.post(ContentDeletedEvent(this.asInstanceOf[FileResource]))
    }
  }

  def lastModification = Some(file.lastModified())

}
