package org.eknet.publet.content

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.05.13 20:43
 */
sealed trait ResourceType {
  def isFolder: Boolean
  def isContent: Boolean
  def isDynamic: Boolean
}
case object Container extends ResourceType {
  val isFolder = true
  val isContent = false
  val isDynamic = false
}
case object File extends ResourceType {
  val isFolder = false
  val isContent = true
  val isDynamic = false
}
case object Dynamic extends ResourceType {
  val isFolder = false
  val isContent = false
  val isDynamic = true
}
