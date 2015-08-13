package org.snappas.configcleaner

import org.scalatest.FlatSpec
import org.scalatest.prop.TableDrivenPropertyChecks

import scala.collection.immutable.SortedMap

class ConfigCleanerTest extends FlatSpec with TableDrivenPropertyChecks{



  val expectedInputListOfCfgLines = List(
    """seta cl_125kz "1"""",
    """seta cg_fov "100"""",
    """seta r_vertexLight "1"""",
    """seta ch_graphs "1"""",
    """bind g "weapon 1"""",
    """seta r_speeds "0"""",
    """bind x "doSomething;vstr script1"""",
    """set script1 "vstr script2"""",
    """set script2 "vstr script3"""",
    """set script3 "vstr script4;vstr script5"""",
    """set script4 "doSomething"""",
    """set script5 "doSomethingElse"""",
    """alias clansset "GM_qlfc_clans"""",
    """alias someAlias "doSomething""""
  )

  val expectedOutputListOfDefaultCvars = List(
    ("cg_fov", "100")
  )

  val expectedOutputListOfNondefaultCvars = List(
    ("r_vertexLight", "1")
  )

  val expectedOutputListOfAlias = List(
    ("clansset","GM_qlfc_clans"),
    ("someAlias","doSomething")
  )

  val expectedOutputListOfValidCvars = List(
    ("cg_fov","100"),
    ("r_vertexLight","1")
  )

  val expectedOutputListOfInvalidCvars = List(
    ("ch_graphs","1"),
    ("cl_125kz","1"),
    ("r_speeds","0"),
    ("script1","vstr script2"),
    ("script2","vstr script3"),
    ("script3","vstr script4;vstr script5"),
    ("script4","doSomething"),
    ("script5","doSomethingElse")
  )

  val expectedUpdatedInvalidCvars = List(
    ("ch_graphs","1"),
    ("cl_125kz","1"),
    ("r_speeds","0")
  )

  val expectedOutputMapOfBinds = SortedMap(
    "g" -> "weapon 1",
    "x" -> "doSomething;vstr script1"
  )

  val expectedOutputListOfScriptsInUse = SortedMap(
    "script1"->"vstr script2",
    "script2"->"vstr script3",
    "script3"->"vstr script4;vstr script5",
    "script4"->"doSomething",
    "script5"->"doSomethingElse"
  )

  val cfgEdgeCase = List(
    """bind y "vstr weirdScript""",
    """set doesNotExist """""
  )

  "given a list of cfg lines, get list of aliases" should "produce a list of aliases" in {
    assert(ConfigCleaner.extractListOfAlias(expectedInputListOfCfgLines) == expectedOutputListOfAlias)
  }

  "given a list of cfg lines, get list which are not valid names" should "produce 2 lists" in {
    assert(
      ConfigCleaner.validAndInvalidCvars(ConfigCleaner.generateDefaultCvarsMap, expectedInputListOfCfgLines)
        == (expectedOutputListOfValidCvars, expectedOutputListOfInvalidCvars)
    )
  }

  "given a list of cfg lines, get a list of binds" should "produce a list of binds" in {
    assert(ConfigCleaner.generateBindsMap(expectedInputListOfCfgLines) == expectedOutputMapOfBinds)
  }



  "given a list of cfg lines, find which scripts are used" should "produce a list of scripts" in {
    assert(
      ConfigCleaner.validScripts(expectedOutputMapOfBinds,expectedOutputListOfInvalidCvars)
        == (expectedOutputListOfScriptsInUse,expectedUpdatedInvalidCvars)
    )
  }

  "given a list of cvars and default cvars find which have default value and nondefault value" should "produce 2 lists" in {
    assert(
      ConfigCleaner.nonDefaultCvars(expectedOutputListOfValidCvars, ConfigCleaner.generateDefaultCvarsMap)
        == (expectedOutputListOfNondefaultCvars, expectedOutputListOfDefaultCvars)
    )
  }

  "given a list of cfg lines, produce an alias list, validscripts list, binds list," +
    " nondefaultcvars list, defaultcvars list, invalidcvars list" should "produce 6 lists" in {
    assert(
      ConfigCleaner.generateOutputLists(expectedInputListOfCfgLines) ==
        (expectedOutputListOfAlias, expectedOutputListOfScriptsInUse.toList,
          expectedOutputMapOfBinds.toList, expectedOutputListOfNondefaultCvars,
          expectedOutputListOfDefaultCvars, expectedUpdatedInvalidCvars)
    )
  }

  "given a bind with a reference to nonexistent script" should "produce empty map of scripts and empty list of unused cvars" in {
    assert( ConfigCleaner.validScripts(SortedMap("y" -> "vstr noScript"),Seq()) == (Map(),List()))
  }

  "given a bind with a reference to a script that exists and the script references a nonexistent script" should "produce 1 valid script" in {
    assert( ConfigCleaner.validScripts(SortedMap("y" -> "vstr uselessScript"),Seq(("uselessScript","vstr doesNotExist"))) == (Map("uselessScript" -> "vstr doesNotExist"),List()))
  }



}
