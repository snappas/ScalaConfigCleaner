package org.snappas.configcleaner

object ConfigRegex {
  def extractDefaultCvarFromLine(cvarString: String): (String, String, String) = {
    val defaultRegex = """([SURIALTC ]{8})\s([A-z|0-9]+[_]?)\s"(.*)"""".r
    cvarString match{
      case defaultRegex(flags, cvarName, defaultValue) => (cvarName, defaultValue, flags)
      case default => null
    }
  }

  def extractCvarFromCfgLine(cfgLine: String): (String, String) = {
    val cvarRegex = """(seta|set)\s([A-z|0-9]+[_]?)\s"(.*)"""".r
    cfgLine match {
      case cvarRegex(_, name, value) => (name, value)
      case default => null
    }
  }

  def extractBindFromCfgLine(cfgLine: String): (String, String) = {
    val bindRegex = """(bind)\s([A-z|0-9]+[_]?)\s"(.*)"""".r
    cfgLine match {
      case bindRegex(_, key, command) => (key, command)
      case default => null
    }
  }

  def extractAliasFromCfgLine(cfgLine: String): (String, String) = {
    val aliasRegex = """(alias)\s([A-z|0-9]+[_]?)\s"(.*)"""".r
    cfgLine match {
      case aliasRegex(_, name, value) => (name, value)
      case default => null
    }
  }

  def findAllVstrNamesInALine(line: String) = {
    val vstrRegex = """vstr\s([A-z|0-9|_]+)""".r

    vstrRegex.findAllMatchIn(line).map{
      case vstrRegex(name) => name
    }.toList
  }

  def isClientCvar(flags: String) = {
    !(flags(0) == 'S' || flags(2) == 'R' || flags(3) == 'I' || flags(6) == 'C')
  }

}
