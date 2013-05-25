package org.eknet.publet.scalate

import com.typesafe.config.Config
import org.eknet.publet.content.Path
import org.fusesource.scalate.TemplateEngine

/**
 * @author Eike Kettner eike.kettner@gmail.com
 * @since 12.05.13 13:57
 */
class ScalateSettings(config: Config) {

  import config._
  import collection.JavaConverters._

  val engineConfigs = {
    val all = getConfig("publet.scalate-engine")

    for (entry <- all.root().entrySet().asScala;
         cfg = all.getConfig(entry.getKey)
         if (!cfg.getStringList("patterns").asScala.isEmpty))
    yield entry.getKey -> EngineConfig(
      allowCaching = cfg.getBoolean("allow-caching"),
      escapeMarkup = cfg.getBoolean("escape-markup"),
      allowReload = cfg.getBoolean("allow-reload"),
      importStatements = cfg.getStringList("import-statements").asScala.toList,
      patterns = cfg.getStringList("patterns").asScala.toSet[String].map(s => Path(s)),
      nrInstances = cfg.getInt("nr-of-instances")
    )

  }.toMap

  val scriptCompilerConfig = ScriptCompilerConfig(
    importStatements = getStringList("publet.scalascript-engine.import-statements").asScala.toList,
    patterns = getStringList("publet.scalascript-engine.patterns").asScala.toSet[String].map(s => Path(s))
  )
}

case class ScriptCompilerConfig(importStatements: List[String], patterns: Set[Path])

case class EngineConfig (
   allowCaching: Boolean = false,
   escapeMarkup: Boolean = true,
   allowReload: Boolean = true,
   importStatements: List[String] = Nil,
   patterns: Set[Path] = Set(),
   nrInstances: Int = 1
) {
  def setTo(engine: TemplateEngine) {
    engine.allowCaching = allowCaching
    engine.allowReload = allowReload
    engine.escapeMarkup = escapeMarkup
    engine.importStatements = engine.importStatements ::: importStatements
  }
}
