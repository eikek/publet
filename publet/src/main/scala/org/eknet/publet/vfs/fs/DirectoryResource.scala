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
import org.eknet.publet.vfs.events.ContainerCreatedEvent

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 14:07
 */
class DirectoryResource(dir: File, root: Path, bus: EventBus)
    extends AbstractLocalResource(dir, root, bus) with ContainerResource with Modifyable {

  def children = Option(dir.listFiles()).map(_.map(f => {
    if (f.isDirectory) newDirectory(f, root, bus)
    else newFile(f, root, bus)
  })).getOrElse(Array[Resource]()).toIterable

  def content(name: String) = newFile(new File(dir, name), root, bus)

  def container(name: String) = newDirectory(new File(dir, name), root, bus)

  def child(name: String) = {
    val f = new File(dir, name)
    if (!f.exists) None
    else if (f.isDirectory) Some(newDirectory(f, root, bus))
    else Some(newFile(f, root, bus))
  }

  def create() {
    dir.mkdir()
    bus.post(new ContainerCreatedEvent(this))
  }

  def isWriteable = true

  override def toString = "Directory[" + dir.toString + "]"
}
