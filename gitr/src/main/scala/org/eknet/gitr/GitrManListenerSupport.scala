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

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 10.05.12 02:14
 */
trait GitrManListenerSupport {
  this: GitrMan =>

  abstract class GitrManEvent[T](el: T, source: GitrMan)
  case class CreateRepoEvent(el: GitrRepository, source: GitrMan) extends GitrManEvent[GitrRepository](el, source)
  case class CreateTandemEvent(el: Tandem, source: GitrMan) extends GitrManEvent[Tandem](el, source)

  private var listeners: List[PartialFunction[GitrManEvent[_], Unit]] = Nil

  def addListener(l: PartialFunction[GitrManEvent[_], Unit]) {
    this.listeners ::= l
  }

  private[gitr] def emit[T](ev: GitrManEvent[T]) {
    for (l <- listeners if (l.isDefinedAt(ev))) l(ev)
  }

  private[gitr] def emit[T](repo: GitrRepository): GitrRepository = {
    val ev = CreateRepoEvent(repo, this)
    emit(ev)
    repo
  }

  private[gitr] def emit[T](tandem: Tandem): Tandem = {
    val ev = CreateTandemEvent(tandem, this)
    emit(ev)
    tandem
  }
}
