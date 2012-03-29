package org.eknet.publet.impl

import org.eknet.publet.Uri

/**
 * Supports only `?` and a single `*`
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 13:17
 */
case class Glob(pattern: String) extends Ordered[Glob] {

  //create a regex and match against that
  lazy val regex = ("^"+pattern.replaceAll("\\?", ".?").replaceAll("\\*", ".*")).r

  def matches(uri: Uri): Boolean = regex.findFirstIn(uri.path).isDefined


  def compare(that: Glob) = pattern.length().compare(that.pattern.length())

}
