package org.eknet.publet.source

import org.eknet.publet.{ContentType, FileContent, Path, Content}
import java.io._

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 23:41
 */
class FilesystemPartition(root: File, val name: Symbol) extends Partition {
  
  Predef.ensuring(root != null, "dir must not be null")
  Predef.ensuring(root.isDirectory, "Not a directory")

  if (!root.exists) root.mkdirs()

  def this(root: File) = this(root, 'local)

  def this(path: Path) = this(new File(path.toAbsolute.asString))
  def this(str: String) = this(Path(str))

  def lookup(path: Path) = {
    val file = relativeFile(path)
    if (file.exists) Option(Content(file)) else None
  }

  def create(path: Path, target: ContentType) = {
    val file: File = (Path(root) / path).withExtension(target.extensions.head)
    if (file.exists) Left(new RuntimeException("File already exists"))
    else {
      if(!new File(file.getParent).exists())
        new File(file.getParent).mkdirs()

      file.createNewFile();
      Right(FileContent(file, ContentType.html)) 
    }
  }

  private def relativeFile(path: Path):File = (Path(root) / path)
}
