package org.eknet.publet.auth

import scala.xml.Node

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 11.05.12 11:18
 */

case class RepositoryModel(name: String, tag: RepositoryTag.Value) {

  def toXml = {
    <repository name={name} tag={tag.toString}/>
  }
}

object RepositoryModel {

  def apply(repoNode: Node): RepositoryModel = {
    val name = (repoNode \ "@name").text
    val tag = RepositoryTag.withName((repoNode \ "@tag").toString())
    RepositoryModel(name, tag)
  }
}