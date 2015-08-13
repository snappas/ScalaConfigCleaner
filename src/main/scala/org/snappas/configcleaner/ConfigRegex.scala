package org.snappas.configcleaner

object ConfigRegex {
  def extractDefaultCvar(cvarString: String): (String, String, String) = {
    val defaultRegex = """([SURIALTC ]{8})\s([A-z|0-9]+[_]?)\s"(.*)"""".r
    cvarString match{
      case defaultRegex(flags, cvarName, defaultValue) => (flags, cvarName, defaultValue)
    }
  }

  def extractCvar(cfgLine: String): (String, String) = {
    val cvarRegex = """(seta|set)\s([A-z|0-9]+[_]?)\s"(.*)"""".r
    cfgLine match {
      case cvarRegex(_, name, value) => (name, value)
      case default => null
    }
  }

  def extractBind(cfgLine: String): (String, String) = {
    val bindRegex = """(bind)\s([A-z|0-9]+[_]?)\s"(.*)"""".r
    cfgLine match {
      case bindRegex(_, key, command) => (key, command)
      case default => null
    }
  }

  def extractAlias(cfgLine: String): (String, String) = {
    val aliasRegex = """(alias)\s([A-z|0-9]+[_]?)\s"(.*)"""".r
    cfgLine match {
      case aliasRegex(_, name, value) => (name, value)
      case default => null
    }
  }

  def findVstr(line: String) = {
    val vstrRegex = """vstr\s([A-z|0-9|_]+)""".r

    vstrRegex.findAllMatchIn(line).map{
      case vstrRegex(name) => name
    }.toList
  }

  def defaultClientCvars(cvarString: String): (String, String, Boolean) = {
    val (flags, cvarName, defaultValue) = extractDefaultCvar(cvarString)
    val notClient = flags(0) == 'S' || flags(2) == 'R' || flags(3) == 'I' || flags(6) == 'C'

    (cvarName, defaultValue, !notClient)
  }

}
