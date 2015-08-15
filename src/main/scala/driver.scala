import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import org.snappas.configcleaner.{Command, Config, ConfigCleaner}

import scala.io.Source


object driver{
  var cfg: File = null

  def main(args: Array[String]): Unit = {
    if (args.nonEmpty) {
      cfg = new File(args(0))
    }

    cfg = JavaFXFileDialog.chooseFileWithJavaFXDialog()
    if (cfg == null)
      sys.exit(1)

    var outputText = ""
    outputText += "//Generated from " + cfg.getName + "\n"
    val cfgLines = Source.fromFile(cfg).getLines()

    outputText += generateOutput(cfgLines)

    Files.write(Paths.get("./"+cfg.getName+"-clean.cfg"), outputText.getBytes(StandardCharsets.UTF_8))
    sys.exit()

  }

  def generateOutput(cfg: Iterator[String]): String = {
    val defaultCvars = Source.fromURL(getClass.getResource("/cvars.txt")).getLines()
    val config = new Config(cfg, defaultCvars)
    var output = ""

    val aliases = config.aliasCommands
    val binds = config.bindCommands
    val invalidCommands = ConfigCleaner.invalidCvars(config.cvarMap, config.defaultCvarsMap)
    val validCvars = ConfigCleaner.validCvars(config.cvarMap, config.defaultCvarsMap)
    val clientCvars = ConfigCleaner.clientCvars(validCvars, config.defaultCvarsMap)
    val (scripts, invalidCvars) = ConfigCleaner.validScripts(binds, invalidCommands)
    val (defaultCommands, modifiedCommands) = ConfigCleaner.defaultNondefaultCvars(clientCvars)


    if (binds.nonEmpty) {
      output += "\n//Binds\n"
      output += addCollectionToOutput(binds, "bind")
    }

    if (aliases.nonEmpty) {
      output += "\n//Aliases\n"
      output += addCollectionToOutput(aliases, "alias")
    }

    if (scripts.nonEmpty) {
      output += "\n//Scripts in use\n"
      output += addCollectionToOutput(scripts, "set")
    }

    if (modifiedCommands.nonEmpty) {
      output += "\n//Modified commands\n"
      output += addCollectionToOutputWithDefault(modifiedCommands, "seta")
    }

    if (defaultCommands.nonEmpty) {
      output += "\n//Default commands\n"
      output += addCollectionToOutput(defaultCommands, "seta")
    }

    if (invalidCvars.nonEmpty) {
      output += "\n//Invalid commands/unused scripts\n"
      output += addCollectionToOutput(invalidCvars, "//seta")
    }

    output
  }


  def addCollectionToOutput(collection: Map[String, Command], prefix: String) = {
    var output = ""
    collection.toSeq.map(_._2).sortBy(_.name).foreach {
      command =>
        output += prefix + " " + command.name + " \"" + command.value + "\"\n"
    }
    output
  }

  def addCollectionToOutputWithDefault(collection: Map[String, Command], prefix: String) = {
    var output = ""
    collection.toSeq.map(_._2).sortBy(_.name).foreach {
      command =>
        output += prefix + " " + command.name + " \"" + command.value + "\"" +
          " //Default: \"" + command.defaultValue + "\"\n"
    }
    output
  }

}
