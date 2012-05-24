package org.eknet.publet.auth

import xml.PermissionModel

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 12:56
 */
trait PubletAuth {

  def getAllRepositories: Seq[RepositoryModel]
  def getRepository(repository: String): Option[RepositoryModel] = getAllRepositories.find(t => t.name==repository)

  def getAllUser: Seq[User]
  def findUser(login: String): Option[User]

  def getPolicy(login: String): Policy
  def getPolicy(user: User): Policy

  def updateUser(user: User)
  def updateRepository(repo: RepositoryModel)
  def updatePermission(perm: PermissionModel)

  def getResourceConstraints(uri: String): Option[ResourceConstraint]
  def addResourceConstraint(rc: ResourceConstraint)
}

object PubletAuth {

  val Empty = new PubletAuth {
    def findUser(login: String) = None
    def getPolicy(login: String) = Policy.Empty
    def getPolicy(user: User) = Policy.Empty
    def getAllUser = Seq[User]()
    def getAllRepositories = Seq[RepositoryModel]()
    def updateUser(user: User) {}
    def updateRepository(repo: RepositoryModel) {}
    def updatePermission(perm: PermissionModel) {}
    def getResourceConstraints(uri: String) = None
    def addResourceConstraint(rc: ResourceConstraint) {}
  }
}