package org.eknet.publet.vfs


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.04.12 18:25
 */
trait Modifyable {

  /**
   * Deletes this resource. If this is a container
   * it must be empty before deleting it.
   *
   */
  def delete()

  /** Creates this resource, if it does not exist, Does nothing
   * if it already exists.
   *
   * The parent container must exist.
   *
   */
  def create()

}

trait ModifyableContainer {
  this: Modifyable with Container =>

  /**
   * If this is a container try to create a content resource
   * at the specified path relative to this resource. All
   * paths in between are created if not existent.
   *
   * @param path
   * @return
   */
  def createContent(path: Path): ContentResource = {
    val c = createContainer(path.parent).content(path.name.fullName)
    c.ifModifyable(_.create()).getOrElse(sys.error("resource not mofifieable"))
    c
  }

  def createContainer(path: Path): ContainerResource = {
    create()
    container(path.head) match {
      case mc: ModifyableContainer => mc.createContainer(path.tail)
      case _ => sys.error("Cannot create next path")
    }
  }
}
