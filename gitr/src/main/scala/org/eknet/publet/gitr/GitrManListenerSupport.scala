package org.eknet.publet.gitr

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
