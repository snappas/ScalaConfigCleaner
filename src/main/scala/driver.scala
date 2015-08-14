import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}

import org.snappas.configcleaner.{Config, ConfigCleaner}

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
    outputText += newline("//Generated from " + cfg.getName)
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


    if (binds.nonEmpty)
      output += newline("\n//Binds")
    binds.toSeq.map(_._2).sortBy(_.name).foreach {
      bind =>
        output += newline("bind " + bind.name + " \"" + bind.value + "\"")
    }

    if (aliases.nonEmpty)
      output += newline("\n//Aliases")
    aliases.toSeq.map(_._2).sortBy(_.name).foreach {
      alias =>
        output += newline("alias " + alias.name + " \"" + alias.value + "\"")
    }

    if (scripts.nonEmpty)
      output += newline("\n//Scripts in use")
    scripts.toSeq.map(_._2).sortBy(_.name).foreach {
      script =>
        output += newline("set " + script.name + " \"" + script.value + "\"")
    }

    if (modifiedCommands.nonEmpty)
      output += newline("\n//Modified commands")
    modifiedCommands.toSeq.map(_._2).sortBy(_.name).foreach {
      command =>
        output += newline("seta " + command.name + " \"" + command.value + "\"" + " //Default: \"" + command.defaultValue + "\"")
    }

    if (defaultCommands.nonEmpty)
      output += newline("\n//Default commands")
    defaultCommands.toSeq.map(_._2).sortBy(_.name).foreach {
      command =>
        output += newline("seta " + command.name + " \"" + command.value + "\"")
    }

    if (invalidCvars.nonEmpty)
      output += newline("\n//Invalid commands/unused scripts")
    invalidCvars.toSeq.map(_._2).sortBy(_.name).foreach {
      invalid =>
        output += newline("//seta " + invalid.name + " \"" + invalid.value + "\"")
    }

    output
  }

  def newline(text: String) = text + "\n"

}
