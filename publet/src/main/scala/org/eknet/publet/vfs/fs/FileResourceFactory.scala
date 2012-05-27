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
import org.eknet.publet.vfs.{ContentResource, ContainerResource, Path}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.04.12 01:18
 */
trait FileResourceFactory {


  protected def newDirectory(f: File, root: Path): ContainerResource = new DirectoryResource(f, root)

  protected def newFile(f: File, root: Path): ContentResource = new FileResource(f, root)

}
