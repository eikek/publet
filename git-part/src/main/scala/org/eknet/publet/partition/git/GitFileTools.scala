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

package org.eknet.publet.partition.git

import java.io.File

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 11:45
 */
trait GitFileTools {

  protected def file: File
  protected def root: GitPartition

  def getFilePattern = {
    file.getAbsolutePath.substring(root.tandem.workTree.getWorkTree.getAbsolutePath.length()+1)
  }

  def git = root.tandem.workTree.git

  def lastCommit = {
    val path = getFilePattern
    val logs = git.log().addPath(path).call().iterator()
    if (logs.hasNext)
      Some(logs.next())
    else
      None
  }

}
