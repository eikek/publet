package org.eknet.publet.webapp.assets

import org.eknet.publet.content.{Source, DynamicContent}
import org.eknet.publet.webapp.PubletWebSettings
import scala.concurrent.Future

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 30.05.13 22:39
 */
class AssetLoader(settings: PubletWebSettings) extends DynamicContent {

  private val groupRegex = "[0-9a-zA-Z\\.\\-,]+".r

  def name = "assetloader.js"

  def create(params: Map[String, String]) = {
    val groups = params.get("groups").getOrElse("default")
    if (groupRegex.pattern.matcher(groups).matches()) {
      val urlBase = settings.urlFor(settings.assetsBasePath.absoluteString)
      Future.successful{ Some(Source.js(jsCode(groups, urlBase))) }
    } else {
      Future.failed(new IllegalArgumentException("Invalid group name(s)"))
    }
  }

  private def jsCode(groups: String, urlBase: String) = s"""(function() {
     |
     |  function isUrlParameter(name) {
     |    return decodeURI((RegExp('[?&]'+name).exec(location.search))) !== "null";
     |  }
     |
     |  var groups = "$groups";
     |  var urlBase = "$urlBase/";
     |
     |  function createHeadElement() {
     |    var heads = document.getElementsByTagName("head");
     |    if (heads.length == 0) {
     |      var head = document.createElement("head");
     |      var parent = document.getElementsByTagName("html")[0];
     |      if (parent.children.length > 0) {
     |        parent.insertBefore(parent.children[0], head);
     |      } else {
     |        parent.appendChild(head);
     |      }
     |    }
     |  }
     |
     |  createHeadElement();
     |  var head = document.getElementsByTagName("head")[0];
     |
     |  function loadAsset(filename, type) {
     |    var fileref = null;
     |    if (type == "js") {
     |      fileref = document.createElement('script');
     |      fileref.setAttribute("type","text/javascript");
     |      fileref.setAttribute("src", filename);
     |    }
     |    else if (type == "css") {
     |      fileref = document.createElement("link");
     |      fileref.setAttribute("rel", "stylesheet");
     |      fileref.setAttribute("type", "text/css");
     |      fileref.setAttribute("href", filename)
     |    }
     |    if (fileref != null) {
     |      head.appendChild(fileref);
     |    }
     |  }
     |
     |  if (isUrlParameter("debug")) {
     |    //don't load compressed files. wait for the request, to retain
     |    //order of childs in the current head element.
     |    var xmlhttp =  new XMLHttpRequest();
     |    xmlhttp.open('GET', urlBase + 'groupAll/?name='+groups, false);
     |    xmlhttp.onreadystatechange = function() {
     |      if (xmlhttp.readyState == 4) {
     |        var files = JSON.parse(xmlhttp.response);
     |        Array.prototype.map.call(files.css, function(el) {
     |          loadAsset(el, "css");
     |        });
     |        Array.prototype.map.call(files.js, function(el) {
     |          loadAsset(el, "js");
     |        });
     |      }
     |    };
     |    xmlhttp.send(null);
     |  } else {
     |    loadAsset(urlBase+'compressed/js?name='+groups, "js");
     |    loadAsset(urlBase+'compressed/css?name='+groups, "css");
     |  }
     |})();""".stripMargin
}
