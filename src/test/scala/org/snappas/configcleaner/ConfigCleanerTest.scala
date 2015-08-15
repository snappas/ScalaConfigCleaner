package org.snappas.configcleaner

import org.scalatest.{BeforeAndAfter, FlatSpec}

import scala.io.Source

class ConfigCleanerTest extends FlatSpec with BeforeAndAfter {
  var config: Config = _
  var cfgLines: Iterator[String] = _
  var defaultCvarLines: Iterator[String] = _


  before {
    cfgLines = Iterator(
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
      """alias someAlias "doSomething"""",
      """seta sv_ranked "0""""
    )

    defaultCvarLines = Source.fromURL(getClass.getResource("/cvars.txt")).getLines()

    config = new Config(cfgLines, defaultCvarLines)
  }

  "collection of valid cvars" should " be nonempty" in {
    assert(ConfigCleaner.validCvars(config.cvarMap, config.defaultCvarsMap).nonEmpty)
  }

  "collection of valid cvars" should " contain cg_fov" in {
    assert(ConfigCleaner.validCvars(config.cvarMap, config.defaultCvarsMap).get("cg_fov").isDefined)
  }

  "collection of invalid cvars" should " contain cl_125kz" in {
    assert(ConfigCleaner.invalidCvars(config.cvarMap, config.defaultCvarsMap).get("cl_125kz").isDefined)
  }

  "collection of valid scripts " should " contain script1" in {
    val invalidCvars = ConfigCleaner.invalidCvars(config.cvarMap, config.defaultCvarsMap)
    assert(ConfigCleaner.validScripts(config.bindCommands, invalidCvars)._1.get("script1").isDefined)
  }

  "default cvars in cfg" should " contain cg_fov" in {
    val validCvars = ConfigCleaner.validCvars(config.cvarMap, config.defaultCvarsMap)
    assert(ConfigCleaner.defaultNondefaultCvars(validCvars)._1.get("cg_fov").isDefined)
  }

  "nondefault cvars in cfg" should " contain r_vertexLight" in {
    val validCvars = ConfigCleaner.validCvars(config.cvarMap, config.defaultCvarsMap)
    assert(ConfigCleaner.defaultNondefaultCvars(validCvars)._2.get("r_vertexLight").isDefined)
  }

  "client cvars in cfg" should " not include sv_ranked" in {
    val validCvars = ConfigCleaner.validCvars(config.cvarMap, config.defaultCvarsMap)
    assert(ConfigCleaner.clientCvars(validCvars, config.defaultCvarsMap).get("sv_ranked").isEmpty)
  }






}
