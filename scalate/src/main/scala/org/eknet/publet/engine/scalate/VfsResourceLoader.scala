package org.eknet.publet.engine.scalate

import org.eknet.publet.Publet
import org.fusesource.scalate.util.{Resource, ResourceLoader}
import org.eknet.publet.vfs.{Container, ContentResource, ContainerResource, Path}
import org.fusesource.scalate.TemplateEngine
import javax.management.remote.rmi._RMIConnection_Stub

/**
 * A resource loader that uses [[org.eknet.publet.vfs.Container]] to lookup
 * the uri if it fails using the delegate.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 19.05.12 00:54
 */
class VfsResourceLoader(container: Container, delegate: Option[ResourceLoader]) extends ResourceLoader {

  def this(publet: Publet, resourceLoader: ResourceLoader) = this(publet.rootContainer, Some(resourceLoader))

  def resource(uri: String) = {
    delegate.flatMap(_.resource(uri)) orElse {
      container.lookup(Path(uri))
        .collect({ case c:ContentResource => new TemplateResource(uri, c)})
    }
  }
}

object VfsResourceLoader {

  def install(engine: TemplateEngine, publet: Publet) {
    val vfsl = new VfsResourceLoader(publet, engine.resourceLoader)
    engine.resourceLoader = vfsl
  }
}
