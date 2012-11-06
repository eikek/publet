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

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.transport.RefSpec
import java.io.{FileWriter, File}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 00:58
 */
trait GitrTandem {
  this: GitrManListenerSupport with GitrMan =>

  def createTandem(name: RepositoryName, branch: String = "master"): Tandem = {
    val bare = getOrCreate(name, true)
    createTandemFromBare(bare, branch)
  }

  def createTandemFromBare(bare: GitrRepository, branch: String = "master"): Tandem = {
    if (!bare.isBare) sys.error("Cannot create a tandem from a non-bare repository: "+ bare.name)
    val split = splitName(bare.name)
    val ws = clone(bare.name, split._2, false)

    //create some initial content
    val readmeFile = "_README.txt"
    val writer = new FileWriter(new File(ws.getWorkTree, readmeFile))
    writer.write("Initial Readme")
    writer.close()
    ws.git.add().addFilepattern(readmeFile).setUpdate(false).call()
    ws.git.commit()
      .setAuthor("Publet Install", "none@none")
      .setMessage("Initial commit.")
      .setAll(true)
      .call()

    //rename branch, in case different from "master"
    val git = Git.wrap(ws)
    if (branch != "master") {
      git.branchRename()
        .setOldName("master")
        .setNewName(branch)
        .call()
    }
    //push initial content to bare repo
    git.push()
      .setRemote("origin")
      .setRefSpecs(new RefSpec(branch))
      .call()

    //add remote tracking
    val cfg = ws.getConfig
    cfg.setString("branch", branch, "remote", "origin")
    cfg.setString("branch", branch, "merge", "refs/heads/"+ branch)
    cfg.setBoolean("gitr", null, "tandem", true)
    cfg.save()

    val bareCfg = bare.getConfig
    bareCfg.setBoolean("gitr", null, "tandem", true)
    bareCfg.save()

    emit(Tandem(split._1, bare, ws))
  }

  private def splitName(name: RepositoryName): (RepositoryName, RepositoryName) = {
    val base = name.name
    val ws = base + "_gitrcheckout"
    (RepositoryName(base), RepositoryName(ws))
  }

  def getTandem(name: RepositoryName): Option[Tandem] = {
    get(name) filter (_.isTandem) map { bare =>
      val split = splitName(name)
      val workname = split._2
      val workTree = get(workname).get
      Tandem(split._1, bare, workTree)
    }
  }
}
