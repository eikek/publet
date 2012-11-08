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

import org.eknet.publet.vfs._
import org.eknet.publet.auth.xml.XmlResource
import scala.xml.{Node, Elem}
import org.eknet.gitr.RepositoryName
import java.util.concurrent.locks.ReentrantReadWriteLock
import org.apache.shiro.SecurityUtils
import org.eknet.publet.auth.store.{DefaultAuthStore, UserStore}
import org.eknet.publet.Publet
import com.google.inject.{Provider, Inject, Singleton}
import com.google.inject.name.Named

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.11.12 16:10
 */
@Singleton
class XmlRepositoryStore @Inject() (@Named("contentroot") container: Provider[Container], authm: DefaultAuthStore) extends RepositoryStore {

  lazy val data = new XmlData(container.get()
    .lookup(Publet.allIncludesPath / "config" / "repositories.xml")
    .collect({ case c: ContentResource => c})
    .getOrElse(Resource.emptyContent(ResourceName("repositories.xml"), ContentType.xml)), authm)

  def findRepository(name: RepositoryName): Option[RepositoryModel] =
    data.repositories.get(name.name)

  def allRepositories = data.repositories.values

  def repositoriesByOwner(owner: String) = data.repositories.values.filter(rm => rm.owner == owner)

  def updateRepository(rm: RepositoryModel) = data.modify { model =>
    val old = model.repositories.get(rm.name.name)
    model.repositories = model.repositories + (rm.name.name -> rm)
    old
  }

  def removeRepository(name: RepositoryName) = data.modify { model =>
    val old = model.repositories.get(name.name)
    model.repositories = model.repositories - name.name
    old
  }

}

class XmlData(source: ContentResource, userStore: UserStore) extends XmlResource(source) {

  private val model = new Model

  reload()

  class Model {
    var repositories = Map[String, RepositoryModel]()
  }

  def onLoad(rootElem: Elem) {
    withWriteLock {
      this.model.repositories = (rootElem \ "repository")
        .map(repositoryFromXml(_))
        .map(rm => rm.name.name -> rm)
        .toMap
    }
  }

  def toXml() = {
    <repositories>
      { model.repositories.values.map(rm => repositoryToXml(rm)) }
    </repositories>
  }

  def repositories = withReadLock( model.repositories )

  private def repositoryToXml(rm: RepositoryModel) = {
      <repository name={rm.name.name} tag={rm.tag.toString} owner={rm.owner}/>
  }

  private def repositoryFromXml(repoNode: Node): RepositoryModel = {
    val name = (repoNode \ "@name").text
    val tag = RepositoryTag.withName((repoNode \ "@tag").toString())
    val owner = (repoNode \ "@owner").text
    RepositoryModel(name, tag, owner)
  }

  def modify[A](f: Model => A): A = {
    withWriteLock {
      val r = f(model)
      val login = SecurityUtils.getSubject.getPrincipal.toString
      write(userStore.findUser(login), "Writing repository model data.")
      r
    }
  }
}