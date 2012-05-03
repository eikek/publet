package org.eknet.publet.web.shiro

import collection.JavaConversions._
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.authz.AuthorizationInfo
import org.apache.shiro.subject.{SimplePrincipalCollection, PrincipalCollection}
import org.apache.shiro.authc.{DisabledAccountException, AuthenticationInfo, AuthenticationToken}
import org.eknet.publet.web.WebContext
import org.eknet.publet.web.util.{Session, Request, Key}
import org.apache.shiro.SecurityUtils
import org.eknet.publet.auth.{Policy, User, AuthManager}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 08:14
 */
class UsersRealm(val db: AuthManager) extends AuthorizingRealm {

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
    val p = principals.getPrimaryPrincipal.asInstanceOf[User]
    new PolicyAuthInfo(p)
  }

  class PolicyAuthInfo(user: User) extends AuthorizationInfo {
    def getRoles = user.roles

    private def policy = {
      val op = Option(Security.session.getAttribute("policy")).map(_.asInstanceOf[Policy])
      op.getOrElse {
        val policy = db.policyFor(user)
        SecurityUtils.getSubject.getSession.setAttribute("policy", policy)
        policy
      }
    }

    def getStringPermissions = policy.stringPermissions
    def getObjectPermissions = List()
  }

  class UserAuthInfo(user: User) extends AuthenticationInfo {
    def getPrincipals = new SimplePrincipalCollection(user, "Publet Protected")
    def getCredentials = user.algorithmPassword._2
  }
}
