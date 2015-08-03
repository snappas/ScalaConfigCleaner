import java.io.File
import domain.ConfigCleaner
import scala.io.Source


object driver{
  var cfg: File = null

  def main(args: Array[String]): Unit = {

    while (cfg == null) {
      cfg = JavaFXFileDialog.chooseFileWithJavaFXDialog()
      println("//Generated from "+cfg)
    }
    val cfgLines = Source.fromFile(cfg).getLines.toList
    val rawResult = ConfigCleaner.generateOutputLists(cfgLines)
    val aliases = rawResult._1
    val validScripts = rawResult._2
    val binds = rawResult._3
    val modifiedCvars = rawResult._4
    val defaultCvars = rawResult._5


    if(aliases.nonEmpty) {
      println("//Aliases")
      aliases.foreach { alias =>
        print("alias " + alias.get._1)
        println(" \"" + alias.get._2 + "\"")
      }
    }

    if(validScripts.nonEmpty) {
      println("//Scripts in use")
      validScripts.foreach { script =>
        print("set " + script._1)
        println(" \"" + script._2 + "\"")
      }
    }

    if(binds.nonEmpty) {
      println("//Binds")
      binds.foreach { bind =>
        print("bind " + bind._1)
        println(" \"" + bind._2 + "\"")
      }
    }

    if(modifiedCvars.nonEmpty) {
      println("//Modified Cvars")
      modifiedCvars.foreach { cvar =>
        print("seta " + cvar.get._1)
        println(" \"" + cvar.get._2 + "\"")
      }
    }

    if(defaultCvars.nonEmpty) {
      println("//Default Cvars")
      defaultCvars.foreach { defaultCvar =>
        print("seta " + defaultCvar.get._1)
        println(" \"" + defaultCvar.get._2 + "\"")
      }
    }
    sys.exit()
  }



}
