package org.eknet.publet.auth

import java.io.InputStream
import org.eknet.publet.vfs.{Path, ContentResource}


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.04.12 00:00
 */
class FileAuthManager(userFile: ContentResource, ruleFile: ContentResource) extends AuthManager {

  userFile.ensuring(f => f.exists, "File not readable")
  ruleFile.ensuring(f => f.exists, "File not readable")

  private val parse = new AuthParser

  private def rules: InputStream = ruleFile.inputStream

  private def users: InputStream = userFile.inputStream

  private def loadRules = parse.rules(rules)

  private def loadUsers = parse.principals(users)

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
