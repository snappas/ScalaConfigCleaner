name := "ScalaConfigCleaner"

version := "1.0"

scalaVersion := "2.11.7"

libraryDependencies += "org.scalatest" % "scalatest_2.11" % "3.0.0-M6" % "test"
libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "1.0.4"
libraryDependencies += "org.scala-lang" % "scala-reflect" % "2.11.7"
libraryDependencies += "org.scala-lang" % "scala-library" % "2.11.7"

ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := "<empty>"

fork := true