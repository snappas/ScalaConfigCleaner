import java.io.File
import org.snappas.configcleaner.ConfigCleaner

import scala.io.Source
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets


object driver{
  var cfg: File = null

  def main(args: Array[String]): Unit = {
    if(args.nonEmpty){
      cfg = new File(args(0))
    }

    while (cfg == null) {
      cfg = JavaFXFileDialog.chooseFileWithJavaFXDialog()
    }
    var outputText = ""
    outputText += newline("//Generated from " + cfg.getName)
    val cfgLines = Source.fromFile(cfg).getLines().toList
    val rawResult = ConfigCleaner.generateOutputLists(cfgLines)
    val aliases = rawResult._1
    val validScripts = rawResult._2
    val binds = rawResult._3
    val modifiedCvars = rawResult._4
    val defaultCvars = rawResult._5
    val invalidCvars = rawResult._6


    if(aliases.nonEmpty) {
      outputText += newline("//Aliases")
      aliases.foreach { alias =>
        outputText += newline("alias " + alias._1 + " \"" + alias._2 + "\"")
      }
    }

    if(validScripts.nonEmpty) {
      outputText += newline("//Scripts in use")
      validScripts.foreach { script =>
        outputText += newline("set " + script._1 + " \"" + script._2 + "\"")
      }
    }

    if(binds.nonEmpty) {
      outputText += newline("//Binds")
      binds.foreach { bind =>
        outputText += newline("bind " + bind._1 + " \"" + bind._2 + "\"")
      }
    }

    if(modifiedCvars.nonEmpty) {
      outputText += newline("//Modified Cvars")
      modifiedCvars.foreach { cvar =>
        outputText += newline("seta " + cvar._1 + " \"" + cvar._2 + "\"")
      }
    }

    if(defaultCvars.nonEmpty) {
      outputText += newline("//Default Cvars")
      defaultCvars.foreach { defaultCvar =>
        outputText += newline("seta " + defaultCvar._1 + " \"" + defaultCvar._2 + "\"")
      }
    }

    if(invalidCvars.nonEmpty){
      outputText += newline("//Invalid Cvars")
      defaultCvars.foreach { invalidCvar =>
        outputText += newline("//seta" + invalidCvar._1 + " \"" + invalidCvar._2 + "\"")
      }
    }


    Files.write(Paths.get("./"+cfg.getName+"-clean.cfg"), outputText.getBytes(StandardCharsets.UTF_8))
    sys.exit()
  }

def newline(text: String) = text + "\n"

}
