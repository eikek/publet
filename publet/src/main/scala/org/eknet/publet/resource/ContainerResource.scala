package org.eknet.publet.resource

import org.eknet.publet.Path

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 14:00
 */
trait ContainerResource extends Resource with Container {

  val isContainer = true
}
