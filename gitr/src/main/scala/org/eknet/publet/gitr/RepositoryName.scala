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

package org.eknet.publet.gitr

/**
 * A name of a repository. Can be a simple name
 * or a path, where segments must be separated
 * by a dash `/`
 *
 * @param name
 */
case class RepositoryName(name: String) {

  if (!name.matches("[\\w\\._/\\-]+")) sys.error("invalid repository name:" + name)

  val segments = name.split("/")

  val isDotGit = name.endsWith(".git")

  /** Converts this to a new repository name with the `.git` extension */
  def toDotGit = if (isDotGit) this else RepositoryName(name+".git")

  /** Converts this to a new [[org.eknet.publet.gitr.RepositoryName]] without the `.git` extension. */
  def strip = if (isDotGit) RepositoryName(name.substring(0, name.length-4)) else this
}

object RepositoryName {
  def apply(segments: Traversable[String]):RepositoryName = RepositoryName(segments.mkString("/"))
}