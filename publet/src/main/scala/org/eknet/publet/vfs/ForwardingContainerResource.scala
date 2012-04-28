package org.eknet.publet.vfs

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 28.04.12 19:26
 */
trait ForwardingContainerResource extends ForwadingResource with ContainerResource {

  protected def delegate:ContainerResource

  def children = delegate.children
  def content(name: String) = delegate.content(name)
  def container(name: String) = delegate.container(name)
  def child(name: String) = delegate.child(name)
}
