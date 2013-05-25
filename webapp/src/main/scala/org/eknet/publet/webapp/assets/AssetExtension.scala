package org.eknet.publet.webapp.assets

import akka.actor.Props
import org.eknet.publet.webapp.extensions.WebExtension
import org.eknet.publet.webapp.assets.AssetActor._
import org.eknet.publet.content.{Content, Path}
import akka.util.Timeout
import org.eknet.publet.webapp.PubletWeb
import org.eknet.publet.webapp.assets.compress.CompressType
import org.eknet.publet.webapp.assets.AssetActor.GetGroupResources
import org.eknet.publet.webapp.assets.AssetActor.ReplaceGroups
import org.eknet.publet.webapp.assets.AssetActor.AddGroups
import org.eknet.publet.webapp.assets.AssetActor.GetCompressed

/**
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 14.05.13 06:52
 *
 */
trait AssetExtension extends WebExtension {

  val assetRef = webapp.actorOf(Props[AssetActor], "assets")
  assetRef ! AddGroups(StandardAssets)
  initDefaultHighlightJsTheme()

  import akka.pattern.ask
  import org.eknet.publet.webapp.makeResponse

  withRoute {
    val basePath = PubletWeb(system).webSettings.assetsBasePath
    path((basePath.toString + "/groups") / PathElement / Rest) { (group, uri) =>
      complete(findGroupResource("groups", group, uri))
    } ~
    path((basePath.toString + "/compressed") / Rest ) { rest =>
      val uri = Path(rest)
      parameter("name") { params =>
        val kind = if (uri.head == CompressType.js.name) CompressType.js else CompressType.css
        complete(getCompressed(params.split(",").toSet, None, kind))
      } ~
      pass {
        complete {
          findGroupResource("compressed", uri.head, uri.tail.toString)
        }
      }
    } ~
    path((basePath.toString + "/groupJs")) {
      parameter("name") { name =>
        complete(getGroupResourcePaths(name.split(",").toSet, None, CompressType.js))
      }
    } ~
    path((basePath.toString + "/groupCss")) {
      parameter("name") { name =>
        complete(getGroupResourcePaths(name.split(",").toSet, None, CompressType.css))
      }
    }
  }

  private def findGroupResource(prefix: Path, group: String, uri: String) = {
    implicit val timeout = Timeout(2000)
    (assetRef ? Find(prefix / group / uri)).mapTo[Option[Content]].map(makeResponse)
  }

  private def getCompressed(groups: Set[String], path: Option[Path], kind: CompressType.Type) = {
    implicit val timeout = Timeout(10000)
    (assetRef ? GetCompressed(groups, path, kind)).mapTo[Content].map(makeResponse)
  }

  private def getGroupResourcePaths(groups: Set[String], path: Option[Path], kind: CompressType.Type) = {
    implicit val timeout = Timeout(2000)
    import spray.json._
    import DefaultJsonProtocol._

    (assetRef ? GetGroupResources(groups, path, kind)).mapTo[List[Path]]
      .map(plist => plist.map(_.absoluteString).toJson.compactPrint)
  }

  private def initDefaultHighlightJsTheme() {
    val theme = PubletWeb(system).appSettings.highlightTheme
    if (!theme.isEmpty) {
      val resources = StandardAssets.highlightjs.assets.collect {
        case a if (a.resource.name.base == theme) => a.copy(merge = true)
      }
      assetRef ! ReplaceGroups(Seq(Group(StandardAssets.highlightjs.name, assets = resources)))
    }
  }
}
