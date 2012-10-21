package org.eknet.publet.webdav

import com.google.inject.AbstractModule
import org.eknet.publet.web.guice.{PubletModule, PubletBinding}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 16.10.12 19:18
 * 
 */
class WebdavModule extends AbstractModule with PubletBinding with PubletModule {

  def configure() {
    binder.bindRequestHandler.toType[WebdavHandlerFactory]
  }

}
