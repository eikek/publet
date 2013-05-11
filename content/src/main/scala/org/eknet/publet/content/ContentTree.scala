package org.eknet.publet.content

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.05.13 22:12
 */
class ContentTree(root: Partition) extends FindBasedPartition with Registry {

  type Key = Path
  type Value = Partition

  def this() = this(EmptyPartition)

  def mount(path: Path, partition: Partition) {
    register(path, partition)
  }
  def unmount(path: Path) {
    unregister(path)
  }

  def find(path: Path) = resolvePartition(path).invoke(p => p.find)

  override  def children = resolvePartition(Path.root).part.children

  override def createFolder(path: Path, info: ModifyInfo) =
    resolvePartition(path).invoke(p => p.createFolder(_, info))

  override def createContent(path: Path, content: Content, info: ModifyInfo) =
    resolvePartition(path).invoke(p => p.createContent(_, content, info))

  override def delete(path: Path, info: ModifyInfo) =
    resolvePartition(path).invoke(p => p.delete(_, info))

  def select(path: Path) = resolvePartition(path).invoke(p => p.select)


  protected case class ResolvedA(path: Path, part: Partition) {
    def invoke[B](f: Partition => Path => B) = f(part)(path)
  }

  protected def resolve(path: Path) = {
    registry.keys.toList.sortBy(- _.size) //sort mounted paths by size desc
      .find(p => p.isEmpty || path.startsWith(p))  //find first with matching path
      .flatMap(p => registry.get(p).map(list => ResolvedA(path.drop(p.size), list.head)))
  }

  private def resolvePartition(path: Path) = {
    resolve(path).getOrElse(ResolvedA(path, root)) //fallback to root
  }

}
