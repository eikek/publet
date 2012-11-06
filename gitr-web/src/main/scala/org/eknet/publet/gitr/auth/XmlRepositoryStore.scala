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

package org.eknet.publet.gitr.auth

import org.eknet.publet.vfs.ContentResource
import org.eknet.publet.auth.xml.XmlResource
import scala.xml.{Node, Elem}
import org.eknet.gitr.RepositoryName

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.11.12 16:10
 */
class XmlRepositoryStore(source: ContentResource) extends RepositoryStore {

  val data = new XmlData(source)

  def findRepository(name: RepositoryName): Option[RepositoryModel] =
    data.repositories.get(name.name)

  def allRepositories = data.repositories.values

  def repositoriesByOwner(owner: String) = data.repositories.values.filter(rm => rm.owner == owner)

  def updateRepository(rm: RepositoryModel) = null

  def removeRepository(name: RepositoryName) = null

}

class XmlData(source: ContentResource) extends XmlResource(source) {

  private var _repositories = Map[String, RepositoryModel]()

  def onLoad(rootElem: Elem) {
    this._repositories = (rootElem \ "repositories" \ "repository")
      .map(repositoryFromXml(_))
      .map(rm => rm.name.name -> rm)
      .toMap
  }

  def toXml() = {
    <repositories>
      { _repositories.values.map(rm => repositoryToXml(rm)) }
    </repositories>
  }

  def repositories = _repositories

  private def repositoryToXml(rm: RepositoryModel) = {
      <repository name={rm.name.name} tag={rm.tag.toString} owner={rm.owner}/>
  }

  private def repositoryFromXml(repoNode: Node): RepositoryModel = {
    val name = (repoNode \ "@name").text
    val tag = RepositoryTag.withName((repoNode \ "@tag").toString())
    val owner = (repoNode \ "@owner").text
    RepositoryModel(name, tag, owner)
  }
}