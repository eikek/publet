package org.eknet.publet.web.shiro

import collection.JavaConversions._
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.subject.{SimplePrincipalCollection, PrincipalCollection}
import org.apache.shiro.authc.{DisabledAccountException, AuthenticationInfo, AuthenticationToken}
import org.eknet.publet.auth.{User, AuthManager}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 08:14
 */
class UsersRealm(db: AuthManager) extends AuthorizingRealm {

  def doGetAuthenticationInfo(token: AuthenticationToken) = {
    val user = token.getPrincipal.toString
    db.getUser(user) match {
      case Some(u) => {
        if (u.enabled) new UserAuthInfo(u)
        else throw new DisabledAccountException("Account disabled.")
      }
      case None => null
    }
  }

  def doGetAuthorizationInfo(principals: PrincipalCollection) = {
    db.getUser(principals.getPrimaryPrincipal.toString) match {
      case Some(user) => new PolicyAuthInfo(user)
      case None => null
    }
  }

  class PolicyAuthInfo(user: User) extends AuthorizationInfo {
    def getRoles = user.roles

    def getStringPermissions = db.policyFor(user.username) match {
      case None => Set[String]()
      case Some(p) => p.permissions.flatMap(_.permissions)
    }

    def getObjectPermissions = List()
  }

  class UserAuthInfo(user: User) extends AuthenticationInfo {
    def getPrincipals = new SimplePrincipalCollection(user.username, "Publet Protected")

    def getCredentials = user.password
  }
}
