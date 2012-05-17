package org.eknet.publet.gitr

/**
 * A name of a repository. Can be a simple name
 * or a path, where segments must be separated
 * by a dash `/`
 *
 * @param name
 */
case class RepositoryName(name: String) {

  if (!name.matches("[\\w\\._/]+")) sys.error("invalid repository name:" + name)

  val segments = name.split("/")

  val isDotGit = name.endsWith(".git")

  def toDotGit = if (isDotGit) this else RepositoryName(name+".git")
}
