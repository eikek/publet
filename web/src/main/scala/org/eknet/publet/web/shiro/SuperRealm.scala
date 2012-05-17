package org.eknet.publet.web.shiro

import scala.collection.JavaConversions._
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.authz.permission.AllPermission
import org.apache.shiro.authz.{Permission, SimpleAuthorizationInfo}
import org.apache.shiro.authc.{SimpleAuthenticationInfo, AuthenticationToken}
import org.apache.shiro.subject.{SimplePrincipalCollection, PrincipalCollection}
import org.eknet.publet.auth.User

/**
 * A super-user realm, that offers a user with all privileges.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 03.05.12 17:27
 */
class SuperRealm extends AuthorizingRealm {
  def doGetAuthenticationInfo(token: AuthenticationToken) = {
    val info = new SimpleAuthenticationInfo()
    info.setCredentials("superadmin".toCharArray)
    val superuser = User("superadmin", "superadmin".toCharArray, None, Set(), Map())
    info.setPrincipals(new SimplePrincipalCollection(superuser, "publet"))
    info
  }

  def doGetAuthorizationInfo(principals: PrincipalCollection) = {
    val info = new SimpleAuthorizationInfo()
    info.setObjectPermissions(Set[Permission](new AllPermission()))
    info
  }
}
