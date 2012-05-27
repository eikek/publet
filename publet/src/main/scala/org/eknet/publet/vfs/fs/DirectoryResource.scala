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

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 14:07
 */
class DirectoryResource(dir: File, root: Path)
    extends AbstractLocalResource(dir, root) with ContainerResource with Modifyable {

  def children = Option(dir.listFiles()).map(_.map(f => {
    if (f.isDirectory) newDirectory(f, root)
    else newFile(f, root)
  })).getOrElse(Array[Resource]()).toIterable

  def content(name: String) = newFile(new File(dir, name), root)

  def container(name: String) = newDirectory(new File(dir, name), root)

  def child(name: String) = {
    val f = new File(dir, name)
    if (!f.exists) None
    else if (f.isDirectory) Some(newDirectory(f, root))
    else Some(newFile(f, root))
  }

  def create() {
    dir.mkdir()
  }

  def isWriteable = true

  override def toString = "Directory[" + dir.toString + "]"
}
