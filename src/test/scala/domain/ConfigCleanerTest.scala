package domain

import org.scalatest.FlatSpec
import org.scalatest.prop.TableDrivenPropertyChecks

import scala.collection.immutable.SortedMap

class ConfigCleanerTest extends FlatSpec with TableDrivenPropertyChecks{
  val cfgLinesTest = Table(("cfgString","cvar","bind","alias"),
    ("""seta r_mapOverBrightCap "255"""", Some(("r_mapOverBrightCap","255")),None,None),
    ("""seta r_bloomIntensity """"", Some(("r_bloomIntensity","")),None,None),
    ("""bind g "weapon 1;cg_drawcrosshair 2;cg_crosshairsize 33;cg_playerlean 1"""",None,Some(("g","weapon 1;cg_drawcrosshair 2;cg_crosshairsize 33;cg_playerlean 1")),None),
    ("""seta headmodel "ranger"""", Some(("headmodel","ranger")),None,None),
    ("""alias clansset "GM_qlfc_clans"""", None,None,Some(("clansset","GM_qlfc_clans"))))

  forAll(cfgLinesTest) {
    (cfgString: String,
     cvar: Option[(String,String)],
     bind: Option[(String,String)],
     alias: Option[(String,String)] ) =>

    "extract cvar from" + cfgString should " be " + cvar in {
      assert(ConfigCleaner.extractCvar(cfgString) == cvar)
    }

    "extract bind from " + cfgString should " be " + bind in {
      assert(ConfigCleaner.extractBind(cfgString) == bind)
    }

    "extract alias from " + cfgString should " be " + alias in {
      assert(ConfigCleaner.extractAlias(cfgString) == alias)
    }

  }

  val quakeCvarOutput = Table(("cvarLine", "flags", "cvarName", "defaultValue","clientCvar"),
    ("""    A  T cg_zoomOutOnDeath "1"""","    A  T","cg_zoomOutOnDeath","1",true),
    ("""S   AL   sv_ranked "0"""","S   AL  ","sv_ranked","0",false),
    ("""  R      sv_paks """"","  R     ","sv_paks","",false),
    ("""      C  r_speeds "0"""","      C ","r_speeds","0",false),
    ("""   I     fs_basegame """"","   I    ","fs_basegame","",false),
    ("""         r_maxPolys "600"""","        ","r_maxPolys","600",true))

  forAll(quakeCvarOutput) {
    (cvarString: String,
     cvarFlags: String,
     cvarName: String,
     defaultValue: String,
     isClientCvar: Boolean ) =>

    "check if " + cvarString + "has expected flags" should " be " + cvarFlags in {
      assert(ConfigCleaner.extractDefaultCvar(cvarString)._1 == cvarFlags)
    }

    "extract client cvars by flags " + cvarName should " be " + isClientCvar in {
      assert(ConfigCleaner.defaultClientCvars(cvarString) == (cvarName, defaultValue, isClientCvar))
    }

  }


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
    Some("cg_fov", "100")
  )

  val expectedOutputListOfNondefaultCvars = List(
    Some("r_vertexLight", "1")
  )

  val expectedOutputListOfAlias = List(
    Some("clansset","GM_qlfc_clans"),
    Some("someAlias","doSomething")
  )

  val expectedOutputListOfValidCvars = List(
    Some("cg_fov","100"),
    Some("r_vertexLight","1")
  )

  val expectedOutputListOfInvalidCvars = List(
    Some("ch_graphs","1"),
    Some("cl_125kz","1"),
    Some("r_speeds","0"),
    Some("script1","vstr script2"),
    Some("script2","vstr script3"),
    Some("script3","vstr script4;vstr script5"),
    Some("script4","doSomething"),
    Some("script5","doSomethingElse")
  )

  val expectedUpdatedInvalidCvars = List(
    Some("ch_graphs","1"),
    Some("cl_125kz","1"),
    Some("r_speeds","0")
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
  "given a binds map, find a list of keys for an action" should "produce a list of keys" in{
    assert(ConfigCleaner.findBindKeysForAction(expectedOutputMapOfBinds, "weapon 1") == List("g"))
  }

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

  "given a cfg line, find script names in it" should "produce list of script names" in {
    assert(ConfigCleaner.findVstr("""set script3 "vstr script4;vstr script5"""") == List("script4","script5"))
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



}
