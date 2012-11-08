/*
 * Copyright 2012 Eike Kettner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eknet.gitr

import java.io.File

/**
 * A name of a repository. Can be a simple name
 * or a path, where segments must be separated
 * by a dash `/`
 *
 * The name is normalized by stripping the `.git`
 * extension.
 *
 * @param reponame the repository name, either with .git or without
 */
final class RepositoryName(reponame: String) {

  reponame.ensuring(RepositoryName.checkName(_), "Invalid repository name:" + reponame)

  val name = if (reponame.endsWith(".git")) reponame.substring(0, reponame.length-4) else reponame
  val segments = name.split("/")

  val nameDotGit = name +".git"

  val fullName = segments.mkString("/")
  val fullNameDotGit = fullName +".git"

  val path = segments.mkString(File.separator)
  val pathDotGit = path + ".git"

  override def equals(o: Any) = o match {
    case orn: RepositoryName => name == orn.name
    case _ => false
  }

  override def hashCode = name.hashCode

  override def toString = "RepositoryName["+ name +"]"
}

object RepositoryName {
  def apply(segments: Traversable[String]):RepositoryName = RepositoryName(segments.mkString("/"))
  def apply(str: String): RepositoryName = new RepositoryName(str)

  def checkName(repoName: String): Boolean = repoName.matches("[\\w\\._/\\-]+")

  implicit def stringToReponame(str: String) = RepositoryName(str)
}