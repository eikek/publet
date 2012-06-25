package org.eknet.publet.web.webdav

import com.bradmcevoy.http._
import com.bradmcevoy.http.Request.Method
import exceptions.BadRequestException
import org.eknet.publet.vfs
import vfs.{Container, ContainerResource, ContentResource, Modifyable}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 25.06.12 21:35
 */
object WebdavResource {

  def apply(f: ContentResource): WebdavFile = new WebdavFile(f)
  implicit def fileToContent(f: WebdavFile): ContentResource = f.resource
  implicit def contentToFile(c: ContentResource): WebdavFile = apply(c)


  def apply(c: ContainerResource): WebdavDirectory = new WebdavDirectory(c)
  implicit def containerToWebdav(c: ContainerResource): WebdavDirectory = apply(c)
  implicit def webdavToContainer(wd: WebdavDirectory): Container = wd.resource

  def apply(c: Container): WebdavContainer = new WebdavContainer(c)

  def apply(r: vfs.Resource): Resource = r match {
    case c: ContentResource => c
    case c: ContainerResource => c
    case _ => sys.error("Unreachable code!")
  }
}