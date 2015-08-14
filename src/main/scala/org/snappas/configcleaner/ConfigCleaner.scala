package org.snappas.configcleaner

import scala.collection.mutable

object ConfigCleaner {

  def validCvars(cvarMap: Map[String, String], defaultCvarsMap: Map[String, (String, String)]) = {
    cvarMap.map { case (k, v) =>
      if (defaultCvarsMap.get(k).isDefined)
        (k, new Command("cvar", k, v, defaultCvarsMap.get(k).get._1))
      else
        (null, null)
    }.filter(_._1 != null)
  }

  def invalidCvars(cvarMap: Map[String, String], defaultCvarsMap: Map[String, (String, String)]) = {
    cvarMap.map { case (k, v) =>
      if (defaultCvarsMap.get(k).isEmpty)
        (k, new Command("cvar", k, v, ""))
      else
        (null, null)
    }.filter(_._1 != null)
  }

  def validScripts(bindCommands: Map[String, Command], invalidCmds: Map[String, Command]) = {
    val scriptQueue = mutable.Queue[String]()
    var validScriptCommands: Map[String, Command] = Map()
    var invalidCommands = invalidCmds

    bindCommands.values.foreach { bind =>
      ConfigRegex.findAllVstrNamesInALine(bind.value).foreach { vstr =>
        scriptQueue.enqueue(vstr)
      }
    }

    while (scriptQueue.nonEmpty) {
      val scriptName = scriptQueue.dequeue()
      if (invalidCommands.get(scriptName).isDefined) {
        val command = invalidCommands.get(scriptName).get
        validScriptCommands += (command.name -> command)
        invalidCommands = invalidCommands - scriptName
        ConfigRegex.findAllVstrNamesInALine(command.value).foreach { vstr =>
          scriptQueue.enqueue(vstr)
        }
      }
    }
    (validScriptCommands, invalidCommands)
  }

  def defaultNondefaultCvars(validCvars: Map[String, Command]) = {
    validCvars.partition(e => e._2.value == e._2.defaultValue)
  }

  def clientCvars(validCvars: Map[String, Command], defaultCvars: Map[String, (String, String)]) = {
    validCvars.filter(e => ConfigRegex.isClientCvar(defaultCvars(e._1)._2))
  }

}
