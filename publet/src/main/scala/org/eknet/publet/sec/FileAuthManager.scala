package org.eknet.publet.sec

import java.io.{BufferedInputStream, InputStream, FileInputStream, File}
import org.eknet.publet.Path


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.04.12 00:00
 */
class FileAuthManager(userFile: File, ruleFile: File) extends AuthManager {

  userFile.ensuring(f => f.canRead && f.isFile, "File not readable")
  ruleFile.ensuring(f => f.canRead && f.isFile, "File not readable")

  private val parse = new AuthParser

  private def rules:InputStream = new BufferedInputStream(new FileInputStream(ruleFile))
  private def users:InputStream = new BufferedInputStream(new FileInputStream(userFile))

  private def loadRules = parse.rules(rules)
  private def loadUsers = parse.principals(users)

  def getUser(name: String) = loadUsers.find(_.username==name)

  def policyFor(username: String): Option[Policy] = {
    getUser(username) map { user =>
      val p = loadRules filterNot { r =>
        r.roles.intersect(user.roles).isEmpty
      }
      new UserPolicy(p.toList)
    }
  }


  class UserPolicy(rules: List[Rule]) extends Policy {

    def hasPerm(resource: Path, perms: Set[String]) = {
      val r = rules filter { r =>
        resource.asString.matches(r.resource) &&
          !r.permissions.intersect(perms).isEmpty
      }
      r.isEmpty
    }
  }

}
