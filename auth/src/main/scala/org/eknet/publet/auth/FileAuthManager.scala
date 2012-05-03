package org.eknet.publet.auth

import org.eknet.publet.vfs.{Path, ContentResource}


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 21.04.12 00:00
 */
class FileAuthManager(userFile: Iterable[ContentResource],
                      ruleFile: Iterable[ContentResource],
                      mappingsFile: Iterable[ContentResource]) extends AuthManager {

  private val parse = new AuthParser

  private def rules = ruleFile.map(_.inputStream).toList
  private def users = userFile.map(_.inputStream).toList
  private def mappings = mappingsFile.map(_.inputStream).toList

  private def loadRules = rules.flatMap(f => parse.permissionRules(f))
  private def loadUsers = users.flatMap(f => parse.principals(f))
  private def loadMappings = mappings.flatMap(f => parse.urlMappings(f))

  def getUser(name: String) = loadUsers.find(_.login == name)

  def policyFor(username: String): Policy = {
    getUser(username) map { policyFor(_) } getOrElse Policy.empty
  }

  def policyFor(user: User) = {
    val p = loadRules filter { r =>
      !r.roles.intersect(user.roles).isEmpty
    }
    Policy(p.toSet)
  }

  def urlMappings = loadMappings
}
