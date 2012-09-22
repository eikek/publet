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

package org.eknet.publet.server

import java.io.File

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.09.12 19:06
 */
private[server] class FileHelper(val path: String) {

  def asFile = new File(path)

  def /(name: String) = if (path.isEmpty) new FileHelper(name)
    else new FileHelper(path + File.separator + name)

}

private[server] object FileHelper {

  implicit def string2Helper(s: String): FileHelper = new FileHelper(s)
  implicit def helper2String(fh: FileHelper): String = fh.path
  implicit def helper2File(fh: FileHelper): File = fh.asFile
}
