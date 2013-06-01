package org.eknet.publet.webapp.assets

import org.eknet.publet.content.{EmptyPath, Path, Name, Glob}
import org.eknet.publet.webapp.assets.Group.GroupMagnet

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 13.05.13 18:30
 */
case class Group(name: String, pathPattern: Glob = Glob("**"), assets: List[Asset] = Nil, dependsOn: Set[String] = Set(), includes: Set[String] = Set()) {

  private def inGroup(r: Asset) = r.inGroup(name)

  def add(r: Asset*) = copy(assets = assets ::: r.map(inGroup).toList)

  def forPath(glob: String) = copy(pathPattern = Glob(glob))

  def require(group: GroupMagnet, next: GroupMagnet*) = copy(dependsOn = dependsOn ++ group.names ++ next.flatMap(_.names))

  def include(group: GroupMagnet, next: GroupMagnet*): Group = copy(includes = includes ++ group.names ++ next.flatMap(_.names))

  def find(path: Path) = assets.filter(_.resource.name == path.fileName) match {
    case Nil => None
    case a :: Nil => Some(a)
    case list => {
      val thisTarget = path.parent match {
        case EmptyPath => None
        case x => Some(x.toString)
      }
      list.find(_.target == thisTarget)
    }
  }
}

object Group {
  case class GroupMagnet(names: Seq[String])
  implicit def fromGroup(g: Group) = GroupMagnet(Seq(g.name))
  implicit def fromString(name: String) = GroupMagnet(Seq(name))
  implicit def fromGroupList(gs: Iterable[Group]) = GroupMagnet(gs.map(_.name).toSeq)
  implicit def fromStringList(gs: Iterable[String]) = GroupMagnet(gs.toSeq)
}
