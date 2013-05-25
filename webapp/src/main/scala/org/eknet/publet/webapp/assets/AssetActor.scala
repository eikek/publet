package org.eknet.publet.webapp.assets

import akka.actor.Actor
import org.eknet.publet.actor.{Publet, Logging}
import org.eknet.publet.content._
import org.eknet.publet.webapp.PubletWeb
import org.eknet.publet.content.Resource.MutableFolder
import org.eknet.publet.webapp.assets.AssetActor._
import org.eknet.publet.webapp.assets.AssetActor.ReplaceGroups
import scala.Some
import org.eknet.publet.webapp.assets.AssetActor.AddGroups
import akka.actor.Status.Success
import org.eknet.publet.webapp.assets.compress.CompressType

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 14.05.13 21:39
 */
class AssetActor extends Actor with Logging {

  private val container = new MutableFolder("root")
  private val groupReg = new GroupRegistry()
  private val tempdir = Publet(context.system).partitionFactory().create("tmp:///assets-compressed")

  private val basePath = PubletWeb(context.system).webSettings.assetsBasePath

  def receive = {
    case AddGroups(groups) => {
      groupReg.setup(groups: _*)
      attachAssets(groups)
      sender ! Success("done")
    }
    case ReplaceGroups(groups) => {
      val replaced = groupReg.replace(groups: _*)
      val names = groups.map(_.name).toSet

      replaced.foreach(g => container.delete(Path("groups") / g.name, ModifyInfo.none))
      container.select("compressed/**").foreach {
        case (p, r: AssetContent) if (names.contains(r.asset.group)) => container.delete(p, ModifyInfo.none)
        case _ =>
      }
      attachAssets(replaced)
      sender ! Success("done")
    }
    case GetCompressed(groups, path, kind) => {
      val fname = groups.toList.sorted.mkString("_") + "." + kind.toString
      val file = tempdir.find(fname).map(_.asInstanceOf[Content]) getOrElse {
        val assets = groupReg.getSources(groups, path, kind.filter)
        val f = compress.createCompressedFile(fname, assets)
        tempdir.createContent(Path.root, f, ModifyInfo.none)
        f
      }
      sender ! file
    }
    case GetGroupResources(groups, path, kind) => {
      val filter = (a: Asset) => a.merge && kind.filter(a)
      val assets = groupReg.getSources(groups, path, filter)
      val paths = assets.map(a => Path("groups") / a.group / subFolder(a) / a.resource.name)
      sender ! (paths.map(p => basePath / p))
    }
    case Find(path) => {
      sender ! container.find(path)
    }
  }

  private def attachAssets(groups: Iterable[Group]) {
    for (g <- groups; asset <- g.assets) {
      val path = Path("groups") / g.name / subFolder(asset)
      val res = assetResource(asset)
      container.createContent(path, res, ModifyInfo.none)
      container.createContent(Path("compressed") / subFolder(asset), res, ModifyInfo.none)
    }
  }

  private def subFolder(asset: Asset) = {
    asset.target match {
      case Some(path) => path
      case None => asset.resource.name.contentType match {
        case Some(ContentType(_, "css", false)) => "css"
        case Some(ContentType(_, "javascript", false)) => "js"
        case Some(ContentType("image", _, _)) => "img"
        case _ => "other"
      }
    }
  }

}

object AssetActor {

  private class AssetContent(val asset: Asset) extends Content {
    def name = asset.resource.name
    def inputStream = asset.resource.inputStream
    override def length = asset.resource.length
    override def lastModification = asset.resource.lastModification
  }
  private def assetResource(a: Asset) = new AssetContent(a)

  /**
   * Sets a group with resources. If the group is already
   * defined, the resources are added to it.
   * @param groups
   */
  case class AddGroups(groups: Seq[Group])

  /**
   * Replaces all resources of an existing group with the resources
   * of the given group. If `require` and `includes` are non empty
   * they are used in favor for the existing ones.
   * @param groups
   */
  case class ReplaceGroups(groups: Seq[Group])

  case class GetCompressed(groups: Set[String], path: Option[Path], kind: CompressType.Type)

  case class GetGroupResources(groups: Set[String], path: Option[Path], kind: CompressType.Type)

  case class Find(path: Path)
}