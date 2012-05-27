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

package org.eknet.publet.gitr

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 00:59
 */
case class Tandem(name: RepositoryName, bare: GitrRepository, workTree: GitrRepository) {

  //todo check for force-update
  def updateWorkTree() = workTree.git.pull().call()

  def pushToBare() = workTree.git.push().call()

  def branch = workTree.getBranch

}

object Tandem {

  implicit def tandemToRepo(tandem: Tandem): GitrRepository = tandem.bare
}
