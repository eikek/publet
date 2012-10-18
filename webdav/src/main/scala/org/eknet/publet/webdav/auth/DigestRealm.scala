package org.eknet.publet.webdav.auth

import com.google.inject.{Inject, Singleton}
import org.eknet.publet.web.shiro.UsersRealm
import org.eknet.publet.Publet
import com.google.common.eventbus.EventBus
import org.eknet.publet.auth.PubletAuth
import org.apache.shiro.authc.AuthenticationToken
import org.apache.shiro.realm.AuthorizingRealm
import org.apache.shiro.subject.PrincipalCollection
import org.apache.shiro.authc.credential.CredentialsMatcher
import com.google.inject.name.Named

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 16.10.12 11:39
 * 
 */
@Singleton
class DigestRealm @Inject() (userrealm: UsersRealm, @Named("digest") matcher: CredentialsMatcher) extends AuthorizingRealm {

  setCredentialsMatcher(matcher)

  override def supports(token: AuthenticationToken) = {
    token.isInstanceOf[DigestAuthenticationToken]
  }

  def doGetAuthenticationInfo(token: AuthenticationToken) = userrealm.doGetAuthenticationInfo(token)

  def doGetAuthorizationInfo(principals: PrincipalCollection) = userrealm.doGetAuthorizationInfo(principals)
}
