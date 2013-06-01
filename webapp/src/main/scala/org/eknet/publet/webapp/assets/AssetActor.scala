package org.eknet.publet.webapp.assets

import akka.actor.Actor
import org.eknet.publet.actor.{Publet, Logging}
import org.eknet.publet.content._
import org.eknet.publet.webapp.PubletWeb
import org.eknet.publet.webapp.assets.AssetActor._
import scala.Some
import org.eknet.publet.webapp.assets.compress.CompressType
import akka.agent.Agent

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 14.05.13 21:39
 */
class AssetActor(assetReg: Agent[GroupRegistry]) extends Actor with Logging {

  private val tempdir = Publet(context.system).partitionFactory().create("tmp:///assets-compressed")
  private val basePath = PubletWeb(context.system).webSettings.assetsBasePath

  def receive = {
    case GetCompressed(groups, path, kind) => {
      val fname = groups.toList.sorted.mkString("_") + "." + kind.toString
      val file = tempdir.find(fname).map(_.asInstanceOf[Content]) getOrElse {
        val assets = assetReg().getSources(groups, path, kind.filter)
        val f = compress.createCompressedFile(fname, assets)
        tempdir.createContent(Path.root, f, ModifyInfo.none)
        f
      }
      sender ! file
    }
    case GetGroupResources(groups, path, kind) => {
      val filter = (a: Asset) => a.merge && kind.filter(a)
      val assets = assetReg().getSources(groups, path, filter)
      val paths = assets.map(a => Path("groups") / a.group / subFolder(a) / a.resource.name)
      sender ! (paths.map(p => basePath / p))
    }
    case Find(path) => {
      // 1. /basepath/groups/[groupname]/somepath?/file.ext
      // 1. /basepath/[groupname]/somepath?/file.ext
      // 3. /basepath/compressed/otherpath?/file.ext  (searches all)
      import org.eknet.publet.content._
      val resource = path match {
        case "groups" / tail => findAssetInGroup(tail.head, tail.tail).map(_.resource)
        case "compressed" / tail => tempdir.find(tail.fileName).orElse(findAsset(tail).map(_.resource))
        case _ => findAsset(path).map(_.resource)
      }
      sender ! resource
    }
  }

  private def findAssetInGroup(name: String, path: Path) = {
    assetReg().getGroup(name).flatMap(_.find(path))
  }

  private def findAsset(path: Path) = {
    assetReg().getGroups.flatMap(_.find(path.fileName)).headOption
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

  case class GetCompressed(groups: Set[String], path: Option[Path], kind: CompressType.Type)

  case class GetGroupResources(groups: Set[String], path: Option[Path], kind: CompressType.Type)

  case class Find(path: Path)
}