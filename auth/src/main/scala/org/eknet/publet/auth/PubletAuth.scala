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

import xml.PermissionModel

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 12:56
 */
trait PubletAuth {

  def getAllRepositories: Seq[RepositoryModel]

  /**
   * Finds a repository model as specified in the database
   * or returns a default model with tag=open if not found.
   *
   * @param repository
   * @return
   */
  def getRepository(repository: String): RepositoryModel = {
    val name = if (repository.endsWith(".git")) repository.substring(0, repository.length-4) else repository
    getAllRepositories.find(t => t.name==name) getOrElse RepositoryModel(name, RepositoryTag.open, "")
  }

  def getAllUser: Seq[User]
  def findUser(login: String): Option[User]

  def getAllPermissions: Set[PermissionModel]

  /**
   * Returns all groups that are defined.
   *
   * @return
   */
  def getAllGroups: Set[String]
  def getPolicy(login: String): Policy
  def getPolicy(user: User): Policy

  def updateUser(user: User)

  /**
   * Replaces any existing model with the given model, if
   * the repository name matches. If no such repository model
   * exists yet, it is added.
   *
   * @param repo
   */
  def updateRepository(repo: RepositoryModel)
  def removeRepository(repoName: String)

  def updatePermission(perm: PermissionModel)
  def removePermission(group: String, perm: Permission)

  def getResourceConstraints(uri: String): Option[ResourceConstraint]
  def addResourceConstraint(rc: ResourceConstraint)
}

object PubletAuth {

  val Empty = new PubletAuth {
    def findUser(login: String) = None
    def getPolicy(login: String) = Policy.Empty
    def getPolicy(user: User) = Policy.Empty
    def getAllPermissions = Set.empty
    def getAllUser = Seq[User]()
    def getAllRepositories = Seq[RepositoryModel]()
    def getAllGroups = Set()
    def updateUser(user: User) {}
    def updateRepository(repo: RepositoryModel) {}
    def removeRepository(repoName: String) {}
    def updatePermission(perm: PermissionModel) {}
    def removePermission(group: String, perm: Permission) {}
    def getResourceConstraints(uri: String) = None
    def addResourceConstraint(rc: ResourceConstraint) {}
  }

}