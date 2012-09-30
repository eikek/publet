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

package org.eknet.publet.server

import java.io.{IOException, FileFilter, File}
import java.nio.file.{FileVisitResult, Path, SimpleFileVisitor, Files}
import java.nio.file.attribute.BasicFileAttributes

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 20.09.12 19:06
 */
class FileHelper(val path: String) {

  def asFile = new File(path)

  def asFileIfPresent = asFile match {
    case x if (x.exists()) => Some(x)
    case _ => None
  }

  def /(name: String) = if (path.isEmpty) new FileHelper(name)
    else new FileHelper(path + File.separator + name)

}

object FileHelper {

  implicit def file2Helper(f: File): FileHelper = new FileHelper(f.getAbsolutePath)
  implicit def string2Helper(s: String): FileHelper = new FileHelper(s)
  implicit def helper2String(fh: FileHelper): String = fh.path
  implicit def helper2File(fh: FileHelper): File = fh.asFile

  def entries(f: File, filter:File=>Boolean):List[File] = f :: (if (f.isDirectory) listFiles(f, filter).toList.flatMap(entries(_, filter)) else Nil)
  private def listFiles(f:File, filter:File=>Boolean): Array[File] = f.listFiles(filter) match {
    case null => Array[File]()
    case o@_ => o
  }

  implicit def fun2Filter(f:File => Boolean): FileFilter = new FileFilter {
    def accept(pathname: File) = f(pathname)
  }

  /**
   * Deletes the given file and, if it is a directory, it deletes all
   * entries.
   *
   * @param file
   */
  def deleteRecursice(file: File) {
    Files.walkFileTree(file.toPath, new SimpleFileVisitor[Path] {
      override def visitFile(file: Path, attrs: BasicFileAttributes) = {
        Files.delete(file)
        FileVisitResult.CONTINUE
      }
      override def postVisitDirectory(dir: Path, exc: IOException) = {
        if (exc == null) {
          Files.delete(dir)
          FileVisitResult.CONTINUE
        } else {
          FileVisitResult.TERMINATE
        }
      }
    })
  }
}
