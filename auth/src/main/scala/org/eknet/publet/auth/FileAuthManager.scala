package org.eknet.publet.auth

import org.eknet.publet.vfs.{Path, ContentResource}


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.04.12 00:00
 */
class FileAuthManager(userFile: Iterable[ContentResource], ruleFile: Iterable[ContentResource]) extends AuthManager {
  def this(userFile: ContentResource, ruleFile: ContentResource) = this(Seq(userFile), Seq(ruleFile))

  private val parse = new AuthParser

  private def rules = ruleFile.map(_.inputStream)
  private def users = userFile.map(_.inputStream)

  private def loadRules = rules.flatMap(f => parse.rules(f))
  private def loadUsers = users.flatMap(f => parse.principals(f))

  def getUser(name: String) = loadUsers.find(_.username == name)

  def policyFor(username: String): Option[Policy] = {
    getUser(username) map {
      user =>
        val p = loadRules filterNot {
          r =>
            r.roles.intersect(user.roles).isEmpty
        }
        new UserPolicy(p.toList)
    }
  }


  class UserPolicy(rules: List[Rule]) extends Policy {

    def hasPerm(resource: Path, perms: Set[String]) = {
      val r = rules filter {
        r =>
          resource.asString.matches(r.resource) &&
            !r.permissions.intersect(perms).isEmpty
      }
      r.isEmpty
    }

    def permissions = rules.toSet
  }

}
