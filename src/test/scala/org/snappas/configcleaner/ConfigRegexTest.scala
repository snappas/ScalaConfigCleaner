package org.snappas.configcleaner

import org.scalatest.FlatSpec
import org.scalatest.prop.TableDrivenPropertyChecks

class ConfigRegexTest extends FlatSpec with TableDrivenPropertyChecks{
  val cfgLinesTest = Table(("cfgString","cvar","bind","alias"),
    ("""seta r_mapOverBrightCap "255"""", ("r_mapOverBrightCap","255"),null,null),
    ("""seta r_bloomIntensity """"", ("r_bloomIntensity",""),null,null),
    ("""bind g "weapon 1;cg_drawcrosshair 2;cg_crosshairsize 33;cg_playerlean 1"""",null,("g","weapon 1;cg_drawcrosshair 2;cg_crosshairsize 33;cg_playerlean 1"),null),
    ("""seta headmodel "ranger"""", ("headmodel","ranger"),null,null),
    ("""alias clansset "GM_qlfc_clans"""", null,null,("clansset","GM_qlfc_clans")))

  forAll(cfgLinesTest) {
    (cfgString: String,
     cvar: (String,String),
     bind: (String,String),
     alias: (String,String) ) =>

      "extract cvar from" + cfgString should " be " + cvar in {
        assert(ConfigRegex.extractCvarFromCfgLine(cfgString) == cvar)
      }

      "extract bind from " + cfgString should " be " + bind in {
        assert(ConfigRegex.extractBindFromCfgLine(cfgString) == bind)
      }

      "extract alias from " + cfgString should " be " + alias in {
        assert(ConfigRegex.extractAliasFromCfgLine(cfgString) == alias)
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

      "check if " + cvarString + " has expected flags" should " be " + cvarFlags in {
        assert(ConfigRegex.extractDefaultCvarFromLine(cvarString)._3 == cvarFlags)
      }

      "extract client cvars by flags " + cvarName should " be " + isClientCvar in {
        assert(ConfigRegex.isClientCvar(cvarFlags) == isClientCvar)
      }

  }

  "given a cfg line, find script names in it" should " produce list of script names" in {
    assert(ConfigRegex.findAllVstrNamesInALine( """set script3 "vstr script4;vstr script5"""") == List("script4", "script5"))
  }
}
