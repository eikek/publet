package org.eknet.publet.web

import org.eknet.publet.Publet
import org.eknet.publet.partition.git.GitPartition
import shiro.Security
import template.StandardEngine
import javax.servlet.ServletContext
import org.eknet.publet.web.WebContext._
import grizzled.slf4j.Logging
import util.Key

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 26.04.12 20:33
 */
trait WebPublet {

  def publet: Publet

  def gitPartition: GitPartition

  def standardEngine: StandardEngine

  def servletContext: ServletContext
}

object WebPublet extends Logging {
  private val webPubletKey = Key[WebPublet]("org.eknet.web.publet")

  def setup(sc: ServletContext, ext:List[WebExtension]): WebPublet = {
    val publ = new DefaultWebPublet(sc, ext)
    Security.init(publ)
    sc.setAttribute(webPubletKey.name, publ)
    publ
  }

  def apply(sc: ServletContext): WebPublet = sc.getAttribute(webPubletKey.name).asInstanceOf[WebPublet]
  def apply(): WebPublet = WebContext().service(webPubletKey)

  def close(sc:ServletContext) {
    val wp = WebPublet(sc)
    wp.gitPartition.close()
    sc.removeAttribute(publetAuthManagerKey.name)
    wp.servletContext.removeAttribute(webPubletKey.name)
  }

  private class DefaultWebPublet(val servletContext: ServletContext, exts: List[WebExtension]) extends WebPublet {

    private val triple = PubletFactory.createPublet()
    exts.foreach(_.onStartup(this, servletContext))

    def publet = triple._1
    def gitPartition = triple._2
    def standardEngine = triple._3
  }
}