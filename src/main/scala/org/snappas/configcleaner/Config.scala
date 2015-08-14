package org.snappas.configcleaner

class Config(cfgLines: Iterator[String], defaultLines: Iterator[String]) {
  val configLines = cfgLines.toList
  val defaultsLines = defaultLines.toList
  val cvarMap = cvarCommands(configLines)
  val defaultCvarsMap = createDefaultCvarsMap(defaultsLines)
  val bindCommands = bindsCommands(configLines)
  val aliasCommands = aliasesCommands(configLines)

  def cvarCommands(configLines: List[String]) = {
    configLines.map(ConfigRegex.extractCvarFromCfgLine)
      .filter(_ != null)
      .toMap[String, String]
  }


  def createDefaultCvarsMap(defaultCvars: List[String]) = {
    defaultCvars.map(ConfigRegex.extractDefaultCvarFromLine)
      .filter(_ != null)
      .map(e => (e._1, (e._2, e._3)))
      .toMap[String, (String, String)]
  }

  def bindsCommands(configLines: List[String]): Map[String, Command] = {
    configLines.map(ConfigRegex.extractBindFromCfgLine)
      .filter(_ != null)
      .toMap[String, String]
      .map { case (k, v) =>
      (k, new Command("bind", k, v, ""))
    }
  }

  def aliasesCommands(configLines: List[String]): Map[String, Command] = {
    configLines.map(ConfigRegex.extractAliasFromCfgLine)
      .filter(_ != null)
      .toMap[String, String]
      .map(alias => alias._1 -> new Command("alias", alias._1, alias._2, ""))
  }


}
