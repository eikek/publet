package org.eknet.publet.source

import tools.nsc.io.{File, Directory}
import org.eknet.publet.{ContentType, FileContent, Path, Content}

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 28.03.12 23:41
 */
class FilesystemPartition(root: Directory, val name: Symbol) extends Partition {
  
  Predef.ensuring(root != null, "dir must not be null")
  Predef.ensuring(root.isValid, "Not a directory")

  if (!root.exists) root.createDirectory(force = true, failIfExists = true)

  def this(root: Directory) = this(root, 'local)

  def this(path: Path) = this(Directory(path.toAbsolute.asString))
  def this(str: String) = this(Path(str))

  def lookup(path: Path) = {
    val file = relativeFile(path)
    if (file.exists) Option(Content(file)) else None
  }

  def create(path: Path, target: ContentType) = {
    val file: File = (Path(root) / path).withExtension(target.extensions.head)
    if (file.exists) Left(new RuntimeException("File already exists"))
    else {
      if(!file.parent.exists)
        file.parent.createDirectory(force = true, failIfExists = true)
      file.touch(); 
      Right(FileContent(file, ContentType.html)) 
    }
  }

  private def relativeFile(path: Path):File = (Path(root) / path)
}
