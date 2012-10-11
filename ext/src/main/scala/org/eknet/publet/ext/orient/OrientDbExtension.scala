package org.eknet.publet.ext.orient

import org.eknet.publet.web.EmptyExtension
import com.orientechnologies.orient.core.Orient

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 23.06.12 16:51
 */
class OrientDbExtension extends EmptyExtension {

  override def getModule = Some(OrientModule)

  override def onShutdown() {
    //this really shuts down all orient databases!
    //normally, this is called within a jvm shutdown hook. but
    //that would prevent reloading webapps without server restart
    Orient.instance.shutdown()
  }
}
