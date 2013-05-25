package org.eknet.publet.webapp.assets

import org.eknet.publet.content.{Glob, Path}
import collection.mutable
import com.typesafe.scalalogging.slf4j.Logging
import org.eknet.publet.actor.{utils, PubletSettings}
import org.eknet.publet.webapp.PubletWebSettings

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 13.05.13 22:56
 */
class GroupRegistry extends Logging {
  private val graph = collection.mutable.Map[String, Group]()

  def setup(groups: Group*) {
    //find group in tree or create new node
    for (group <- groups) {
      val next = graph.get(group.name).map(g => merge(g, group)) getOrElse group
      graph.put(group.name, next)
    }
  }

  def replace(groups: Group*): Seq[Group] = {
    val replaced = for (
      g <- groups;
      n = graph.get(g.name) if (n.isDefined)
    ) yield replace(n.get, g)

    replaced.foreach(n => graph.put(n.name, n))
    replaced
  }

  def getGroups = graph.values.toList.sortBy(g => g.name)


  /**
   * Returns all sources registered with the given group in the
   * correct order.
   *
   * @param group
   * @return
   */
  def getSources(group: Iterable[String], path: Option[Path], predicate: Asset => Boolean) =
    collectSources(group, path, predicate)

  private def collectSources(groups: Iterable[String], path: Option[Path], predicate: Asset => Boolean) = {
    val roots = groups.map(group => graph.get(group).getOrElse(sys.error(s"Group $group not found.")))

    def collect(groups: Set[Group], next: Group => Set[String]): Set[Group] = {
      val dependants = groups.flatMap(g => next(g).map(name => graph(name)))
      if (dependants.isEmpty) groups else (groups ++ dependants ++ collect(dependants, next))
    }

    val tree = collect(collect(roots.toSet, _.includes), _.dependsOn).map(g => g.name -> g.dependsOn).toMap
    val sorted = utils.toposortLayers(tree) match {
      case Left(tree) => sys.error("Unable to sort asset tree. Remaining: "+ tree)
      case Right(list) => list.flatten.map(n => graph(n))
    }

    val filtered = path match {
      case Some(p) => sorted.filter(g => g.pathPattern.matches(p.absoluteString))
      case None => sorted
    }

    filtered.flatMap(_.assets.filter(predicate))
  }

  private def merge(self: Group, other: Group): Group = {
    if (other.pathPattern != Glob("**")
      && other.pathPattern != self.pathPattern) {
      throw new IllegalArgumentException("There already exists a group '"+self.name+"' with a different path")
    }

    Group(self.name,
      self.pathPattern,
      other.assets ::: self.assets,
      other.dependsOn ++ self.dependsOn,
      other.includes ++ self.includes)
  }

  private def replace(self: Group, other: Group): Group = {
    val replaced = self.assets.map(r => other.assets.find(_.resource.name == r.resource.name).getOrElse(r))
    Group(self.name,
      if (other.pathPattern != Glob("**")) other.pathPattern else self.pathPattern,
      replaced,
      if (other.dependsOn.isEmpty) self.dependsOn else other.dependsOn,
      if (other.includes.isEmpty) self.includes else other.includes
    )
  }
}
