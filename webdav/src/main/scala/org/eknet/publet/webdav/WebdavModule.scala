package org.eknet.publet.webdav

import auth.{DigestCredentialsMatcher, DigestRealm}
import com.google.inject.AbstractModule
import org.eknet.publet.web.guice.{PubletModule, PubletBinding}
import org.apache.shiro.authc.credential.CredentialsMatcher
import com.google.inject.name.Names

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 16.10.12 19:18
 * 
 */
class WebdavModule extends AbstractModule with PubletBinding with PubletModule {

  def configure() {
    binder.set[CredentialsMatcher].annotatedWith(Names.named("digest")).toType[DigestCredentialsMatcher]

    binder.bindRequestHandler.toType[WebdavHandlerFactory]
    binder.bindRealm.toType[DigestRealm]
    bind(classOf[DigestCredentialsMatcher])
  }

}
