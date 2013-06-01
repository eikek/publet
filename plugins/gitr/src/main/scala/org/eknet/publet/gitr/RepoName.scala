package org.eknet.publet.gitr

import org.eknet.publet.content.Path

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 09.05.13 01:24
 */
final case class RepoName(name: Path) {

  require(name.fileName.fullName.matches("""[\w\._\- ]+"""), """Filename must match [\w\._\- ]+""")

  val simpleName = name.sibling(name.fileName.withExtension(""))
  val dotGit = name.sibling(name.fileName.withExtension("git"))

}
