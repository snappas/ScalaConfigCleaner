package org.snappas.configcleaner

import scala.collection.AbstractSeq
import scala.collection.immutable.{SortedMap, TreeMap}
import scala.io.Source

object ConfigCleaner {

  def generateDefaultCvarsMap = {
    var defaultCvarMap: SortedMap[String, (String, Boolean)] = TreeMap()
    val collectionOfDefaultCvarsLines: Iterator[String] = Source.fromURL(getClass.getResource("/cvars.txt")).getLines()
    val result = collectionOfDefaultCvarsLines.map(ConfigRegex.defaultClientCvars)
    result.foreach { element =>
      defaultCvarMap += (element._1 -> (element._2, element._3))
    }
    defaultCvarMap
  }

  def generateBindsMap(cfgLines: AbstractSeq[String]) = {
    var bindsMap: SortedMap[String,String] = TreeMap()
    val result = cfgLines.map(ConfigRegex.extractBind).filter(_ != null)
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
      var scriptNames = ConfigRegex.findVstr(bind)
      while(scriptNames.nonEmpty){
        val (result,rest) = updatedInvalidCvars.partition(e => e._1.compareTo(scriptNames.head) == 0)
        if(result.nonEmpty) {
          validVstr += (result.head._1 -> result.head._2)
          updatedInvalidCvars = rest
          var subVstr = ConfigRegex.findVstr(result.head._2)
          while (subVstr.nonEmpty) {
            val (subResult, subRest) = updatedInvalidCvars.partition(e => e._1.compareTo(subVstr.head) == 0)
            if (subResult.nonEmpty)
              validVstr += (subResult.head._1 -> subResult.head._2)
            updatedInvalidCvars = subRest
            subVstr = subVstr.tail
            if (subVstr.isEmpty && subResult.nonEmpty)
              subVstr = ConfigRegex.findVstr(subResult.head._2)
          }
        }
        scriptNames = scriptNames.tail
      }
    }
    (validVstr,updatedInvalidCvars)
  }

  
  def extractListOfAlias(listOfCfgLines: AbstractSeq[String]) = {
    listOfCfgLines.map(ConfigRegex.extractAlias).filter(_ != null).sortBy(_._1)
  }


  def nonDefaultCvars(validCvars: Iterable[(String,String)], defaultCvarMap: SortedMap[String,(String, Boolean)]) = {
    val (nonDefaultCvars, defaultCvars) = validCvars.partition(cfgCvar =>
      defaultCvarMap.getOrElse(cfgCvar._1, ("", ""))._1 != cfgCvar._2)

    (nonDefaultCvars, defaultCvars)
  }

  def validAndInvalidCvars(defaultCvarMap: SortedMap[String, (String, Boolean)], listOfCfgLines: AbstractSeq[String]) = {
    val listOfCfgCvars = listOfCfgLines.map(ConfigRegex.extractCvar).filter(_ != null)
    val result = listOfCfgCvars.partition(cfgCvar =>
      defaultCvarMap.getOrElse(cfgCvar._1,("","")._1) != null &&
      defaultCvarMap.getOrElse(cfgCvar._1,("",false))._2
    )

    val validCvars = result._1.filter(_ != null).sortBy(_._1)
    val invalidCvars = result._2.filter(_ != null).sortBy(_._1)

    (validCvars, invalidCvars)
  }





}
