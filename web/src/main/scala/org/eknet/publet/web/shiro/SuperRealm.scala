package org.eknet.publet.web.shiro

import scala.collection.JavaConversions._
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.authz.permission.AllPermission
import org.apache.shiro.authz.{Permission, SimpleAuthorizationInfo}
import org.apache.shiro.authc.{SimpleAuthenticationInfo, AuthenticationToken}
import org.apache.shiro.subject.{SimplePrincipalCollection, PrincipalCollection}
import org.eknet.publet.auth.User

/**
 * This realm is used, when security is disabled. It will create a
 * super user with all permissions, so that access checks always
 * succeed.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.05.12 17:27
 */
class SuperRealm extends AuthorizingRealm {
  def doGetAuthenticationInfo(token: AuthenticationToken) = {
    val info = new SimpleAuthenticationInfo()
    info.setCredentials("superadmin".toCharArray)
    val superuser = User("superadmin", "superadmin", "", Set(), "{}superadmin".toCharArray)
    info.setPrincipals(new SimplePrincipalCollection(superuser, "publet"))
    info
  }

  def doGetAuthorizationInfo(principals: PrincipalCollection) = {
    val info = new SimpleAuthorizationInfo()
    info.setObjectPermissions(Set[Permission](new AllPermission()))
    info
  }
}
