package org.eknet.publet.webapp.assets

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 13.05.13 19:11
 */
object StandardAssets extends AssetCollection {

  override def classPathBase = "org/eknet/publet/webapp/includes"

  val mustache = make {
    Group("mustache")
      .add(resource("mustache/mustache.js"))
  }

  val jquery = make {
    Group("jquery")
      .add(resource("jquery/jquery-1.9.1.min.js").noCompress)
      .add(resource("jquery/jquery.form.js"))
  }

  val jqueryMigrate = make {
    Group("jquery.migrate")
      .add(resource("jquery/jquery-migrate-1.1.1.min.js").noCompress)
      .require(jquery)
  }

  val spin =  make {
    Group("spinjs").add(resource("spinjs/spin.min.js").noCompress)
  }

  val spinJquery = make {
    Group("spinjs.jquery")
      .add(resource("spinjs/jquery.spin.js"))
      .require(jquery, spin)
  }

  val highlightjs = make {
    Group("highlightjs")
      .add(resource("highlightjs/highlight.pack.js").noCompress)
      .add(resource("highlightjs/highlight-onload.js"))
      .add(resource("highlightjs/styles/brown_papersq.png").into("style"))
      .add(resource("highlightjs/styles/pojoaque.jpg").into("style"))
      .add(resource("highlightjs/styles/school_book.png").into("style"))
      .add(resource("highlightjs/styles/arta.css").into("style").noMerge)
      .add(resource("highlightjs/styles/ascetic.css").into("style").noMerge)
      .add(resource("highlightjs/styles/brown_paper.css").into("style").noMerge)
      .add(resource("highlightjs/styles/dark.css").into("style").noMerge)
      .add(resource("highlightjs/styles/default.css").into("style").noMerge)
      .add(resource("highlightjs/styles/far.css").into("style").noMerge)
      .add(resource("highlightjs/styles/github.css").into("style").noMerge)
      .add(resource("highlightjs/styles/googlecode.css").into("style").noMerge)
      .add(resource("highlightjs/styles/idea.css").into("style").noMerge)
      .add(resource("highlightjs/styles/ir_black.css").into("style").noMerge)
      .add(resource("highlightjs/styles/magula.css").into("style").noMerge)
      .add(resource("highlightjs/styles/monokai.css").into("style").noMerge)
      .add(resource("highlightjs/styles/pojoaque.css").into("style").noMerge)
      .add(resource("highlightjs/styles/rainbow.css").into("style").noMerge)
      .add(resource("highlightjs/styles/solarized_dark.css").into("style").noMerge)
      .add(resource("highlightjs/styles/solarized_light.css").into("style").noMerge)
      .add(resource("highlightjs/styles/sunburst.css").into("style").noMerge)
      .add(resource("highlightjs/styles/tomorrow.css").into("style").noMerge)
      .add(resource("highlightjs/styles/tomorrow-night.css").into("style").noMerge)
      .add(resource("highlightjs/styles/tomorrow-night-blue.css").into("style").noMerge)
      .add(resource("highlightjs/styles/tomorrow-night-bright.css").into("style").noMerge)
      .add(resource("highlightjs/styles/tomorrow-night-eighties.css").into("style").noMerge)
      .add(resource("highlightjs/styles/vs.css").into("style").noMerge)
      .add(resource("highlightjs/styles/xcode.css").into("style").noMerge)
      .add(resource("highlightjs/styles/zenburn.css").into("style").noMerge)
  }

  val bootstrap = make {
    Group("bootstrap")
      .add(resource("bootstrap/js/bootstrap.min.js").noCompress)
      .add(resource("bootstrap/css/bootstrap.min.css").noCompress)
      .add(resource("bootstrap/img/glyphicons-halflings.png"))
      .add(resource("bootstrap/img/glyphicons-halflings-white.png"))
      .require(jquery)
  }

  //last one adds all above as includes
  val defaultGroup = make {
    Group("default").include(toList)
  }
}
