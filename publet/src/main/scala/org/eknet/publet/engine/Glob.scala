package org.eknet.publet.engine

/**
 * Supports only `?` and a single `*`
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 29.03.12 13:17
 */
protected[engine] case class Glob(pattern: String) extends Ordered[Glob] {

  //create a regex and match against that
  lazy val regex = ("^" + pattern.replaceAll("\\?", ".?").replaceAll("\\*", ".*")).r

  def matches(str: String): Boolean = regex.findFirstIn(str).isDefined


  def compare(that: Glob) = pattern.length().compare(that.pattern.length())

}
