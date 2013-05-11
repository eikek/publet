package org.eknet.publet.content

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 04.05.13 00:21
 */
class EngineRegistry extends Registry {

  type Key = Glob
  type Value = Engine

  def find(path: Path): Option[Engine] = {
    registry.keys.toList.sortBy(- _.pattern.length)
      .find(g => g.matches(path.toString))
      .flatMap(k => headOption(k))
  }
}
