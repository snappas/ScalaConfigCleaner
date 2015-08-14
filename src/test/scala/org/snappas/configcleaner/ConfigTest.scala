package org.snappas.configcleaner

import org.scalatest.{BeforeAndAfter, FlatSpec}

import scala.io.Source

class ConfigTest extends FlatSpec with BeforeAndAfter {


  var config: Config = _
  var cfgLines: List[String] = _

  before {
    cfgLines = List(
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

    val defaultCvarLines = Source.fromURL(getClass.getResource("/cvars.txt")).getLines()
    config = new Config(cfgLines.toIterator, defaultCvarLines)
  }

  "create a config object " should "do some stuff" in {
    assert(config.getClass == classOf[Config])
  }

  "check what binds are created" should "create binds" in {
    assert(config.bindsCommands(cfgLines).nonEmpty)
  }


}
