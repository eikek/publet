package org.eknet.publet.auth

import scala.xml.Node


/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 24.05.12 17:13
 */
case class ResourceConstraint(uriPattern: String, perm: Permission) {
  def toXml = <resourceConstraint name={uriPattern} perm={perm.permString}/>
}

object ResourceConstraint {

  def apply(node: Node): ResourceConstraint = {
    val uri = (node \ "@name").text
    val perm = (node \"@perm").text
    ResourceConstraint(uri, Permission(perm, None))
  }
}
