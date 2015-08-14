
name := "ScalaConfigCleaner"

organization := "org.snappas"

version := "1.0"

scalaVersion := "2.11.7"

scalacOptions += "-optimize"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "3.0.0-M6" % "test"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.4"
libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.7"
libraryDependencies += "org.scala-lang" % "scala-library" % "2.11.7"
libraryDependencies += "org.scoverage" %% "scalac-scoverage-runtime" % "1.1.1"

ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := "<empty>"

//assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false)

fork := true