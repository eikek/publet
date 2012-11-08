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

import com.google.inject.{Singleton, Provides, AbstractModule}
import org.eknet.publet.web.{WebExtension, Config}
import org.eknet.publet.vfs.util.ByteSize
import org.eknet.publet.Publet
import org.eknet.publet.web.guice.{PubletModule, PubletBinding}
import com.google.common.eventbus.EventBus

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 07.10.12 02:22
 */
class ThumbnailModule extends AbstractModule with PubletBinding with PubletModule {

  override def binder() = super.binder()

  def configure() {
    bindExtension.add[ThumbnailExtension]
  }

  @Provides@Singleton
  def createThumbnailer(publet: Publet, config: Config, bus: EventBus): Thumbnailer = {
    val tempDir = config.newStaticTempDir("thumbs")
    val sizeRegex = """((\d+)(\.\d+)?)(.*)""".r
    val options = config("thumbnail.maxDiskSize") flatMap (str => str match {
      case sizeRegex(num, i, p, unit) => {
        if (unit.isEmpty) Some(CacheOptions.maxSize(i.toLong))
        else Some(CacheOptions.maxSize(ByteSize.fromString(unit).toBytes(num.toDouble)))
      }
      case _ => None
    }) orElse {
      config("thumbnail.maxEntries") map (entr => CacheOptions.maxEntries(entr.toInt))
    } getOrElse(CacheOptions.getDefault)

    val tn = new ThumbnailerImpl(publet.mountManager, bus, tempDir, options)
    tn
  }
}
