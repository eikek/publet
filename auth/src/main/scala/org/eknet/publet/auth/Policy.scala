package org.eknet.publet.auth

import org.eknet.publet.vfs.Path

trait Policy {

  def hasPerm(resource: Path, perm: Set[String]): Boolean

  def permissions: Set[Rule]
}
