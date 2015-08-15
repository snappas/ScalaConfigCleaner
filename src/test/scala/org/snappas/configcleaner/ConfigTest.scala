package org.snappas.configcleaner

import org.scalatest.{BeforeAndAfter, FlatSpec}

import scala.io.Source

class ConfigTest extends FlatSpec with BeforeAndAfter {

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

  "create a config object " should "do some stuff" in {
    assert(config.getClass == classOf[Config])
  }

  "check what binds are created" should "create binds" in {
    assert(config.bindsCommands(config.configLines).nonEmpty)
  }

  "create collection of binds" should " contain g" in {
    assert(config.bindsCommands(config.configLines).get("g").isDefined)
  }

  "create collection of binds object" should " be a Command object" in {
    assert(config.bindsCommands(config.configLines).get("g").get.getClass == classOf[Command])
  }

  "create collection of alias objects" should " contain someAlias" in {
    assert(config.aliasesCommands(config.configLines).get("someAlias").isDefined)
  }

  "alias collection Command element for key someAlias value" should " be doSomething" in {
    assert(config.aliasesCommands(config.configLines).get("someAlias").get.value == "doSomething")
  }

  "create collection of default commands" should " be nonempty" in {
    assert(config.createDefaultCvarsMap(config.defaultsLines).nonEmpty)
  }

  "create collection of commands in config" should " be nonempty" in {
    assert(config.cvarCommands(config.configLines).nonEmpty)
  }



}
