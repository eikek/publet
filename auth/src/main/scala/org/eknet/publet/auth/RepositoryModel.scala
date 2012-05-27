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

package org.eknet.publet.auth

import scala.xml.Node

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 11.05.12 11:18
 */

case class RepositoryModel(name: String, tag: RepositoryTag.Value) {

  def toXml = {
    <repository name={name} tag={tag.toString}/>
  }
}

object RepositoryModel {

  def apply(repoNode: Node): RepositoryModel = {
    val name = (repoNode \ "@name").text
    val tag = RepositoryTag.withName((repoNode \ "@tag").toString())
    RepositoryModel(name, tag)
  }
}