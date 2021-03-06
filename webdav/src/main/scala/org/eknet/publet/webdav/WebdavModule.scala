package org.eknet.publet.webdav

import com.google.inject.AbstractModule
import org.eknet.publet.web.guice.{AbstractPubletModule, PubletModule, PubletBinding}
import org.eknet.guice.squire.SquireModule
import org.eknet.publet.vfs.Resource
import org.eknet.publet.web.util.AppSignature

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 16.10.12 19:18
 * 
 */
class WebdavModule extends AbstractPubletModule with PubletBinding with PubletModule {

  def configure() {
    bindRequestHandler.add[WebdavHandlerFactory]
    bindDocumentation(docResource("_webdav.md"))
  }

  private[this] def docResource(names: String*) = names.map("org/eknet/publet/webdav/doc/"+ _).map(Resource.classpath(_)).toList

  val name = "WebDAV"
}
