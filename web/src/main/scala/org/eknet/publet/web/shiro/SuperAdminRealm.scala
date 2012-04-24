package org.eknet.publet.web.shiro

import collection.JavaConversions._
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.authz.{SimpleAuthorizationInfo, AuthorizationInfo}
import org.eknet.publet.web.Config
import org.apache.shiro.subject.{SimplePrincipalCollection, PrincipalCollection}
import org.apache.shiro.authc.{SimpleAuthenticationInfo, DisabledAccountException, AuthenticationToken, AuthenticationInfo}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 22.04.12 08:14
 */
class SuperAdminRealm extends AuthorizingRealm {

    val superadminEnabledProperty = "superadmin.enabled";
    val superadminUserProperty = "superadmin.user";
    val superadminPasswordProperty = "superadmin.password";

  @Override
  protected def doGetAuthorizationInfo(principals: PrincipalCollection): AuthorizationInfo = {
    val confuser = getSuperAdminUser
    if (confuser == principals.getPrimaryPrincipal.toString) {
      val roles = Set("superadmin")
      val info = new SimpleAuthorizationInfo(roles)
      info.addStringPermission("*")
      info
    }
    null
  }

  private def getSuperAdminUser = Config(superadminUserProperty).getOrElse("superadmin")

  @Override
  protected def doGetAuthenticationInfo(token: AuthenticationToken): AuthenticationInfo = {
    val user = token.getPrincipal.toString
    val enabled = Config(superadminEnabledProperty).getOrElse("false").toBoolean
    val superAdmin = Config(superadminUserProperty)
    if (!enabled || superAdmin.isDefined) {
      throw new DisabledAccountException("Super admin account is disabled.")
    }
    if (user.equals(superAdmin)) {
      val principal = new SimplePrincipalCollection(superAdmin, "Super Admin")
      return new SimpleAuthenticationInfo(principal, Config(superadminPasswordProperty))
    }
    null
  }

}
