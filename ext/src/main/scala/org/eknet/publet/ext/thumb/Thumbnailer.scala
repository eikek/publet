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

package org.eknet.publet.ext.thumb

import org.eknet.publet.web.{Config, PubletWeb, PubletWebContext}
import org.eknet.publet.vfs._
import javax.imageio.ImageIO
import java.io.{ByteArrayInputStream, File, ByteArrayOutputStream}
import Path._
import org.eknet.publet.vfs.fs.FilesystemPartition
import java.util.concurrent
import concurrent.Callable
import com.google.common.cache._
import util.ByteSize
import org.eknet.publet.web.util.Key

/**
 * Creating thumbnails of image resources.
 *
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 24.05.12 12:20
 */
class Thumbnailer(mm: MountManager, tempDir: File, options: CacheOptions) {

  val thumbnailPath = "/publet/ext/thumbs/".p

  private val partition = new FilesystemPartition(tempDir, true)
  if (mm.resolveMount(thumbnailPath) == None) {
    mm.mount(thumbnailPath, partition)
  }

  private val sizer = new Weigher[Key, Path] {
    def weigh(key: Key, value: Path) = {
      partition.lookup(value)
        .flatMap(_.asInstanceOf[ContentResource].length).getOrElse(0L).toInt
    }
  }
  private val remover = new RemovalListener[Key, Path] {
    def onRemoval(notification: RemovalNotification[Key, Path]) {
      partition.lookup(notification.getValue)
        .map(_.asInstanceOf[Modifyable].delete())
    }
  }
  private val thumbCache: Cache[Key, Path] = {
    val b = CacheBuilder.newBuilder()
    options.maxSize.map(b.maximumWeight(_)) getOrElse {
      options.maxEntries.map(b.maximumSize(_))
    }
    b.weigher(sizer)
      .removalListener(remover)
      .build()
  }


  /**
   * Creates a thumbnail of the given resource and caches it in the
   * temporary directory.
   *
   * @param c
   * @param maxh
   * @param maxw
   * @return
   */
  def thumbnail(c: ContentResource, maxh: Int, maxw: Int): Path = {
    val key = Key(c.name.fullName, c.lastModification.getOrElse(0), maxw, maxh)
    val file = thumbCache.get(key, callable {
      val img = ImageIO.read(c.inputStream)
      val scaled = ImageScaler.scaleIfNecessary(img, maxw, maxh)
      val baos = new ByteArrayOutputStream()
      ImageIO.write(scaled, "PNG", baos)
      val target = partition.createResource(key.targetName.p)
      target.asInstanceOf[Writeable].writeFrom(new ByteArrayInputStream(baos.toByteArray))
      key.targetName.p
    })
    thumbnailPath / file
  }

  private def callable[V](body: => V) : Callable[V] = new Callable[V] {
    def call() = body
  }

  private case class Key(name: String, lastmod: Long, maxw: Int, maxh: Int) {
    val targetName = name +"-"+ lastmod +"-"+ maxw +"x"+ maxh+ ".png"
  }

}

object Thumbnailer {

  private val thumbnailerKey = Key[Thumbnailer](classOf[Thumbnailer].getName)

  def service() = PubletWebContext.contextMap(thumbnailerKey).getOrElse {
    val tempDir = Config.newStaticTempDir("thumbs")
    val tn = new Thumbnailer(PubletWeb.publet.mountManager, tempDir, CacheOptions())
    PubletWeb.contextMap.put(thumbnailerKey, tn)
    tn
  }
}

/**
 * Options for the thumbnail cache.
 *
 * You can *either* specify a maximum size *or* a maximum entry count. If both
 * are specified, maximum size is prefered.
 *
 * @param maxSize bytes, defaults to 50MiB
 * @param maxEntries number of entries, defaults to [[scala.None]]
 */
case class CacheOptions(maxSize: Option[Long] = Some(ByteSize.mib.toBytes(50)), maxEntries: Option[Int] = None)

