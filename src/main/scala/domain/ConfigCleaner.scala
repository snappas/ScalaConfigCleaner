package domain

import scala.collection.AbstractSeq
import scala.collection.immutable.{SortedMap, TreeMap}
import scala.io.Source

object ConfigCleaner {

  def generateDefaultCvarsMap = {
    var defaultCvarMap: SortedMap[String, (String, Boolean)] = TreeMap()
    val collectionOfDefaultCvarsLines: Iterator[String] = Source.fromURL(getClass.getResource("/cvars.txt")).getLines()
    val result = collectionOfDefaultCvarsLines.map(defaultClientCvars)
    result.foreach { element =>
      defaultCvarMap += (element._1 -> (element._2, element._3))
    }
    defaultCvarMap
  }

  def generateBindsMap(cfgLines: AbstractSeq[String]) = {
    var bindsMap: SortedMap[String,String] = TreeMap()
    val result = cfgLines.map(extractBind).filter(_ != null)
    result.foreach { element =>
      bindsMap += (element._1 -> element._2)
    }
    bindsMap
  }

  def generateOutputLists(cfg: List[String])= {
    val defaultCvarsMap = generateDefaultCvarsMap
    val (validCvars,cvarsNotFound) = validAndInvalidCvars(defaultCvarsMap,cfg)
    val bindsMap = generateBindsMap(cfg)
    val (validScriptsMap,invalidCvars) = validScripts(bindsMap,cvarsNotFound)
    val aliases = extractListOfAlias(cfg)
    val (nonDefaultCvarsMap, defaultCvars) = nonDefaultCvars(validCvars, defaultCvarsMap)

    (aliases.toList, validScriptsMap.toList, bindsMap.toList, nonDefaultCvarsMap.toList, defaultCvars.toList, invalidCvars.toList)
  }

  def validScripts(bindsMap: SortedMap[String, String], invalidCvars: Seq[(String, String)]): (SortedMap[String,String], Seq[(String,String)]) = {
    var validVstr: SortedMap[String,String] = TreeMap()

    var updatedInvalidCvars = invalidCvars
    for(bind <- bindsMap.values){
      var scriptNames = findVstr(bind)
      while(scriptNames.nonEmpty){
        val (result,rest) = updatedInvalidCvars.partition(e => e._1.compareTo(scriptNames.head) == 0)
        if(result.nonEmpty) {
          validVstr += (result.head._1 -> result.head._2)
          updatedInvalidCvars = rest
          var subVstr = findVstr(result.head._2)
          while (subVstr.nonEmpty) {
            val (subResult, subRest) = updatedInvalidCvars.partition(e => e._1.compareTo(subVstr.head) == 0)
            if (subResult.nonEmpty)
              validVstr += (subResult.head._1 -> subResult.head._2)
            updatedInvalidCvars = subRest
            subVstr = subVstr.tail
            if (subVstr.isEmpty && subResult.nonEmpty)
              subVstr = findVstr(subResult.head._2)
          }
        }
        scriptNames = scriptNames.tail
      }
    }
    (validVstr,updatedInvalidCvars)
  }

  
  def extractListOfAlias(listOfCfgLines: AbstractSeq[String]) = {
    listOfCfgLines.map(extractAlias).filter(_ != null).sortBy(_._1)
  }


  def nonDefaultCvars(validCvars: Iterable[(String,String)], defaultCvarMap: SortedMap[String,(String, Boolean)]) = {
    val (nonDefaultCvars, defaultCvars) = validCvars.partition(cfgCvar =>
      defaultCvarMap.getOrElse(cfgCvar._1, ("", ""))._1 != cfgCvar._2)

    (nonDefaultCvars, defaultCvars)
  }

  def validAndInvalidCvars(defaultCvarMap: SortedMap[String, (String, Boolean)], listOfCfgLines: AbstractSeq[String]) = {
    val listOfCfgCvars = listOfCfgLines.map(extractCvar).filter(_ != null)
    val result = listOfCfgCvars.partition(cfgCvar =>
      defaultCvarMap.getOrElse(cfgCvar._1,("","")._1) != null &&
      defaultCvarMap.getOrElse(cfgCvar._1,("",false))._2
    )

    val validCvars = result._1.filter(_ != null).sortBy(_._1)
    val invalidCvars = result._2.filter(_ != null).sortBy(_._1)

    (validCvars, invalidCvars)
  }


  def defaultClientCvars(cvarString: String): (String, String, Boolean) = {
    val (flags, cvarName, defaultValue) = extractDefaultCvar(cvarString)
    val notClient = flags(0) == 'S' || flags(2) == 'R' || flags(3) == 'I' || flags(6) == 'C'

    (cvarName, defaultValue, !notClient)
  }

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

  def findBindKeysForAction(bindsMap: SortedMap[String,String], action: String): List[String] ={
    var listOfKeysForAction = collection.mutable.MutableList[String]()
    val bindRegex = ("""("""+action+""")\b""").r
    bindsMap.keys.foreach {key =>
      val line = bindsMap(key)
      line match {
        case bindRegex(command) => listOfKeysForAction += key
        case default =>
      }
    }
    listOfKeysForAction.toList
  }



}
