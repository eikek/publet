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

package org.eknet.publet.vfs.fs

import java.io._
import org.eknet.publet.vfs._
import com.google.common.eventbus.EventBus
import org.eknet.publet.vfs.events.{ContentWrittenEvent, ContentCreatedEvent}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 01.04.12 14:06
 */
class FileResource(f: File, root: Path, bus: EventBus)
  extends AbstractLocalResource(f, root, bus) with ContentResource with Modifyable with Writeable {

  def inputStream = new FileInputStream(file)


  def writeFrom(in: InputStream, changeInfo: Option[ChangeInfo]) {
    val out = new CloseEventOutStream(new FileOutputStream(file), bus, this, changeInfo)
    Content.copy(in, out, closeIn = false)
  }

  def outputStream: OutputStream = new CloseEventOutStream(new FileOutputStream(file), bus, this, None)

  override def lastModification = Some(file.lastModified())

  def create() {
    file.createNewFile()
    bus.post(ContentCreatedEvent(this))
  }

  override def length = Some(file.length())

  def contentType = ContentType(f)

  override def toString = "File[" + f.toString + "]"

}

private[fs] class CloseEventOutStream(out: OutputStream, bus: EventBus, resource: FileResource, changeInfo: Option[ChangeInfo]) extends OutputStream {
  def write(b: Int) {
    out.write(b)
  }

  override def write(b: Array[Byte]) {
    out.write(b)
  }

  override def write(b: Array[Byte], off: Int, len: Int) {
    out.write(b, off, len)
  }

  override def flush() {
    out.flush()
  }

  override def close() {
    out.close()
    bus.post(ContentWrittenEvent(resource, changeInfo))
  }
}
