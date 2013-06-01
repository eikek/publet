package org.eknet.publet.webapp.assets

import akka.actor.Props
import org.eknet.publet.webapp.assets.AssetActor._
import org.eknet.publet.content.{Content, Path}
import akka.util.Timeout
import org.eknet.publet.webapp.{WebExtension, PubletWeb}
import org.eknet.publet.webapp.assets.compress.CompressType
import org.eknet.publet.webapp.assets.AssetActor.GetGroupResources
import org.eknet.publet.webapp.assets.AssetActor.GetCompressed
import akka.agent.Agent

/**
 * Serves asset resources like javascript, css or image files.
 *
 * 1. /basepath/groups/[groupname]/somepath?/file.ext
 * 2. /basepath/compressed/[js|css]/?name=group1,group2,group2
 * 3. /basepath/compressed/otherpath?/file.ext  (searches all)
 * 4. /basepath/group[Js|Css|All]/?name=group1,group2  (returns JSON of all resources)
 *
 * @author <a href="mailto:eike.kettner@gmail.com">Eike Kettner</a>
 * @since 14.05.13 06:52
 *
 */
trait AssetExtension extends WebExtension {

  val assets = Agent(new GroupRegistry(Map.empty))(system)
  val assetRef = webapp.actorOf(Props(new AssetActor(assets)), "assets")
  assets.send(r => r.setup(StandardAssets: _*))

  import akka.pattern.ask
  import org.eknet.publet.webapp.makeResponse

  withRoute {
    val basePath = PubletWeb(system).webSettings.assetsBasePath
    path((basePath.toString + "/groups") / Rest) { uri =>
      complete(findGroupResource(Path("groups") /uri))
    } ~
    path((basePath.toString + "/compressed") / Rest ) { rest =>
      val uri = Path(rest)
      parameter("name") { params =>
        val kind = if (uri.head.equalsIgnoreCase(CompressType.js.name)) CompressType.js else CompressType.css
        complete(getCompressed(params.split(",").toSet, None, kind))
      } ~
      pass {
        complete {
          findGroupResource(Path("compressed") / uri)
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
    } ~
    path((basePath.toString + "/groupAll")) {
      parameter("name") { name =>
        val groups = name.split(",").toSet
        complete {
          for (
            js <- getGroupResourcePaths(groups, None, CompressType.js);
            css <- getGroupResourcePaths(groups, None, CompressType.css)
          ) yield s"""{ "js": $js, "css" : $css }"""
        }
      }
    } ~
    path(basePath.toString / Rest) { uri =>
      complete(findGroupResource(Path(uri)))
    }
  }

  private def findGroupResource(uri: Path) = {
    implicit val timeout = Timeout(2000)
    (assetRef ? Find(uri)).mapTo[Option[Content]].map(makeResponse)
  }

  private def getCompressed(groups: Set[String], path: Option[Path], kind: CompressType.Type) = {
    implicit val timeout = Timeout(10000)
    (assetRef ? GetCompressed(groups, path, kind)).mapTo[Content].map(makeResponse)
  }

  private def getGroupResourcePaths(groups: Set[String], path: Option[Path], kind: CompressType.Type) = {
    implicit val timeout = Timeout(2000)
    import spray.json._
    import DefaultJsonProtocol._
    val settings = PubletWeb(system).webSettings

    (assetRef ? GetGroupResources(groups, path, kind)).mapTo[List[Path]]
      .map(plist => plist.map(p => settings.urlFor(p.absoluteString)).toJson.compactPrint)
  }

}
