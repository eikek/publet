package org.eknet.publet.postproc

import org.eknet.publet.{Path, Publet, Content}


/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 31.03.12 12:08
 */
trait PostProcessor {

  def process(path: Path, content: Content): Content

  def onInstall(publ: Publet) {}

}
