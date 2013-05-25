package org.eknet.publet.content

import java.nio.file.{Path => JPath, _}
import java.io.IOException
import scala.util.Try
import java.nio.file.attribute.BasicFileAttributes
import java.nio
import FsPath._
import java.net.URI
import scala.Some

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.05.13 22:27
 */
class FsPartition(val directory: JPath, val name: Name) extends Partition with Folder with PartitionSelect {
  if (!Files.exists(directory)) {
    Files.createDirectories(directory)
  }
  require(Files.isDirectory(directory), "The path '"+directory+"' must be a directory!")

  def this(directory: JPath) = this(directory, Name(directory.getFileName.toString))

  private def fileAt(path: Path) = directory.resolve(path.toString)

  override def lastModifcation = Some(Files.getLastModifiedTime(fileAt(directory)).toMillis)

  def children = directory.list.map(FsPartition.createResource)

  def getResourceType(path: Path) = {
    if (Files.isDirectory(fileAt(path))) {
      Some(Container)
    }
    else if (Files.isRegularFile(fileAt(path))) {
      Some(File)
    }
    else {
      None
    }
  }

  def find(path: Path): Option[Resource] = path match {
    case EmptyPath => Some(this)
    case _ => {
      val file = fileAt(path)
      if (file.exists) Some(FsPartition.createResource(file)) else None
    }
  }

  def createFolder(path: Path, info: ModifyInfo) = Try {
    val folder = fileAt(path)
    Files.createDirectories(folder)
    new FsPartition(folder)
  }

  def createContent(path: Path, content: Content, info: ModifyInfo): Try[Content] = Try {
    val folder = fileAt(path)
    val file = folder.resolve(content.name.fullName)
    Files.createDirectories(folder)
    Files.copy(content.inputStream, file, StandardCopyOption.REPLACE_EXISTING)
    new FsContent(file)
  }

  def delete(path: Path, info: ModifyInfo) = Try {
    val file = fileAt(path)
    if (!file.exists) false else {
      if (file.isDirectory) {
        file.deleteDirectory()
      } else {
        Files.deleteIfExists(file)
      }
      true
    }
  }
  override def toString = "FsPartition["+ name +":"+ directory +"]"
  override def equals(that: Any) = that match {
    case p:FsPartition => name.equals(p.name) && directory.equals(p.directory)
    case _ => false
  }
  override def hashCode = name.hashCode() + 11 * directory.hashCode()
}

final case class FsContent(name: Name, file: JPath) extends Content {
  require(!Files.isDirectory(file), "path must not be a directory")
  require(file.exists, s"The file $file does not exist")

  def this(file: JPath) = this(Name(file.getFileName.toString), file)
  def inputStream = file.inputStream
  override def toString = "FsContent["+ name +":"+ file.toString +"]"
  override def lastModification = Some(Files.getLastModifiedTime(file).toMillis)
}

object FsPartition {

  def createResource(name: Name, jpath: JPath): Resource = {
    if (Files.isDirectory(jpath)) new FsPartition(jpath, name) else new FsContent(name, jpath)
  }

  def createResource(jpath: JPath): Resource = {
    val name = Name(jpath.getFileName.toString)
    createResource(name, jpath)
  }

}

/**
 * Creates `FsPartitions` by resolving the uri path against the given
 * directory.
 *
 * @param parent
 */
class SubdirPartitionFactory(parent: JPath) extends PartitionSupplier {
  Files.createDirectories(parent)
  def apply(uri: URI) = {
    val path = Paths.get("/" + uri.getSchemeSpecificPart).toString.substring(1)
    new FsPartition(parent.resolve(path))
  }

  override def toString() = getClass.getSimpleName +"["+ parent +"]"
}

object AbsoluteFsPartitionFactory extends PartitionSupplier {
  def apply(uri: URI) = new FsPartition(Paths.get(uri.getPath))
}

object FsPath {
  class RichPath(val jpath: JPath) {
    def isDirectory = Files.isDirectory(jpath)
    def isRegularFile = Files.isRegularFile(jpath)
    def exists = Files.exists(jpath)
    def list: Iterable[JPath] = new PathIter(jpath)
    def inputStream = Files.newInputStream(jpath)

    def deleteDirectory() {
      Files.walkFileTree(jpath, new SimpleFileVisitor[JPath]() {
        override def visitFile(file: nio.file.Path, attrs: BasicFileAttributes) = {
          Files.delete(file)
          FileVisitResult.CONTINUE
        }

        override def postVisitDirectory(dir: JPath, e: IOException) = {
          if (e == null) {
            Files.delete(dir)
            FileVisitResult.CONTINUE
          } else {
            throw e
          }
        }
      })
      Files.deleteIfExists(jpath)
    }
  }

  private final class PathIter(dir: JPath) extends Iterable[JPath] {
    import collection.JavaConversions._
    def iterator = Files.newDirectoryStream(dir).iterator
  }

  implicit def enrichJPath(jp: JPath): RichPath = new RichPath(jp)
  implicit def unwrapRichJPath(rp: RichPath): JPath = rp.jpath
}