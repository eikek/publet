package org.eknet.publet.web

import org.eknet.publet.Publet
import org.eknet.publet.partition.git.GitPartition
import template.StandardEngine

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 20:33
 */
trait WebPublet {

  def publet: Publet

  def gitPartition: GitPartition

  def standardEngine: StandardEngine

}
