package org.eknet.publet.content

import java.io.InputStream
import scala.annotation.tailrec
import java.net.URL
import org.eknet.publet.content.Source.{StringSource, UrlSource}
import scala.concurrent.{ExecutionContext, Future}
import scala.collection.mutable
import scala.util.{Try, Success, Failure}

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 02.05.13 21:57
 */
sealed trait Resource extends Serializable {

  def name: Name

}

trait Content extends Resource with Source {
  def inputStream: InputStream
  override def contentType: ContentType = name.contentType.getOrElse(ContentType.unknown)
}

trait Folder extends Resource {
  def children: Iterable[Resource]
  def lastModifcation: Option[Long] = None
}

//experimental!
trait DynamicContent extends Resource {
  def create(params: Map[String, String]): Future[Option[Source]]
}

object Resource {

  final case class EmptyFolder(name: Name) extends Folder {
    def children = Nil
  }

  final case class EmptyContent(name: Name) extends Content {
    def inputStream = new InputStream {
      def read() = -1
    }
  }

  final case class SimpleContent(name: Name, source: Source) extends Content {
    def inputStream = source.inputStream
    override def contentType = source.contentType
    override def length = source.length
    override def lastModification = source.lastModification
  }

  class SimpleFolder(val name: Name, children: Iterable[Resource]) extends SimplePartition(children) with Folder {
    /** Creates a new `SimpleFolder` with the additional resource */
    def add(r: Resource) = new SimpleFolder(name, r :: children.toList)
  }
  object SimpleFolder {
    def apply(name: Name, children: Iterable[Resource]) = new SimpleFolder(name, children)
  }

  class MutableFolder(var name: Name, val children: mutable.Buffer[Resource] = mutable.Buffer.empty)
    extends ChildrenBasedPartition with Folder with PartitionSelect {

    def toSimpleFolder = new SimpleFolder(name, children.toList)

    def add(r: Resource*) {
      children.append(r: _*)
    }
    def remove(r: Resource) = {
      val idx = children.indexWhere(_.name == r.name)
      if (idx >= 0) {
        children.remove(idx)
      }
      idx >= 0
    }

    override def createContent(path: Path, content: Content, info: ModifyInfo) = {
      val f = createFolder(path, info)
      f match {
        case Success(mf: MutableFolder) => {
          mf.children.find(_.name == content.name).map(mf.remove)
          mf add content
          Success(content)
        }
        case _ => f.transform(f => Failure(new IllegalArgumentException(s"$f is not writable")), e => Failure(e))
      }
    }

    override def delete(path: Path, info: ModifyInfo) = Try {
      find(path) match {
        case Some(r) => find(path.parent) match {
          case Some(mf: MutableFolder) => mf.remove(r)
          case _ => sys.error(s"Cannot delete resource $r")
        }
        case _ => false
      }
    }

    override def createFolder(path: Path, info: ModifyInfo): Try[MutableFolder] = {
      @tailrec
      def createFolders(mf: MutableFolder, p: Path): Try[MutableFolder] = {
        p match {
          case a / as => mf.find(a) match {
            case Some(f: MutableFolder) => createFolders(f, as)
            case Some(x) => Failure(new IllegalArgumentException(s"cannot create folder in $x"))
            case None => {
              val newf = new MutableFolder(a)
              mf.add(newf)
              createFolders(newf, as)
            }
          }
          case EmptyPath => Success(mf)
        }
      }
      createFolders(this, path)
    }
  }

  def forUrl(url: URL) = SimpleContent(Path(url.getPath).fileName, UrlSource(url))
  def forUrl(name: Name, url: URL) = SimpleContent(name, UrlSource(url))
  def forString(name: Name, str: String) = SimpleContent(name, StringSource(str))

  /**
   * Evaluates `DynamicResources`.
   *
   * @param r
   * @param params
   * @return
   */
  def evaluate(r: Resource, params: Map[String, String])(implicit ec: ExecutionContext): Future[Option[Resource]] = {
    r match {
      case d: DynamicContent => d.create(params).map(s => s.map(SimpleContent(r.name, _)))
      case r => Future.successful(Some(r))
    }
  }
}