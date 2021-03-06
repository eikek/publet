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

package org.eknet.publet.gitr.webui.scripts

import collection.mutable.ListBuffer
import java.text.{DateFormat, SimpleDateFormat}
import org.eknet.publet.web.util.PubletWebContext

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.06.12 15:51
 */
class CommitGroup(val name: String, val title: String, val commits: List[CommitInfo]) extends Ordered[CommitGroup] {

  def toJsonMap = Map("title" -> title, "commits" -> commits.map(_.toMap))

  def compare(that: CommitGroup) = that.name.compare(name)
}


class CommitGroupBuilder {
  import collection.mutable

  private val ymdFormat = new SimpleDateFormat("yyyyMMdd")
  private val titleFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, PubletWebContext.getLocale)

  private val groups = mutable.Map[String, ListBuffer[CommitInfo]]()

  def addByDay(commit: CommitInfo) {
    val date = ymdFormat.format(commit.getCommitDate)
    groups.get(date) getOrElse {
      groups.put(date, new ListBuffer[CommitInfo]())
      groups.get(date).get
    } append(commit)
  }

  def build: List[CommitGroup] = groups
    .map(t => new CommitGroup(t._1, titleFormat.format(t._2.head.getCommitDate), t._2.toList))
    .toList.sorted
}

object CommitGroup {

  def newBuilder = new CommitGroupBuilder

}
