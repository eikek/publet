package org.eknet.publet.auth.xml


import xml.{PrettyPrinter, XML}
import org.eknet.publet.vfs.{Writeable, ContentResource}
import java.io.ByteArrayInputStream
import org.eknet.publet.auth.{Policy, PubletAuth, RepositoryModel, User}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 17.05.12 21:57
 */
class XmlDatabase(source: ContentResource) extends PubletAuth {
  private val prettyPrinter = new PrettyPrinter(90, 2)

  private var lastLoaded: Long = -1
  protected var users: Set[User] = Set()
  protected var repositories:Set[RepositoryModel] = Set()
  protected var permissions:Set[PermissionModel] = Set()

  load()

  /**Reloads database from file, if it has been modified
   * since.
   */
  protected def load() {
    val lastMod = source.lastModification.getOrElse(-1L)
    if (lastMod > lastLoaded) {
      lastLoaded = lastMod
      val rootElem = XML.load(source.inputStream)
      users = (rootElem \ "users" \ "user").map(User(_)).toSet
      repositories = (rootElem \ "repositories" \ "repository").map(RepositoryModel(_)).toSet
      permissions = (rootElem \ "permissions" \ "grant").map(PermissionModel(_)).toSet
    }
  }

  private def write() {
    source match {
      case ws: Writeable => synchronized {
        load()
        val bin = new ByteArrayInputStream(prettyPrinter.format(toXml).getBytes("UTF-8"))
        ws.writeFrom(bin, Some("Permission update"))
        lastLoaded = source.lastModification.getOrElse(-1L)
      }
      case _ =>
    }
  }

  private def toXml = {
    <publetAuth>
      <users>
        { users.map(_.toXml) }
      </users>
      <repositories>
        { repositories.map(_.toXml) }
      </repositories>
      <permissions>
        { permissions.map(_.toXml) }
      </permissions>
    </publetAuth>
  }

  /**
   * Either replaces any existing user (with same login)
   * with the given one, or adds it to the list of users.
   *
   * @param user
   */
  def updateUser(user: User) {
    synchronized {
      val newList = (users - user) + user
      this.users = newList
      write()
    }
  }

  def updateRepository(repo: RepositoryModel) {
    synchronized {
      val newList = (repositories - repo) + repo
      this.repositories = newList
      write()
    }
  }

  def updatePermission(perm: PermissionModel) {
    synchronized {
      val newList = permissions + perm
      this.permissions = newList
      write()
    }
  }

  def findUser(login: String) = users.find(_.login == login)

  def findRepository(name: String) = repositories.find(_.name == name)

  def getAllUser = users.toSeq

  def getAllRepositories = repositories.toSeq

  def getAllPermissions = permissions

  def getPolicy(login: String) = {
    val user = findUser(login).get
    getPolicy(user)
  }

  def getPolicy(user: User) = new Policy {
    def getRoles = user.groups
    def getPermissions = permissions
      .filter(!_.roles.toSet.intersect(user.groups).isEmpty)
      .flatMap(_.toPermissions.values)
      .flatten
      .map(_.permString)
      .toSet
  }
}
