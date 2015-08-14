##Quake Config Cleaner

#### Features: 
- Detects scripts in use (any script with a reference to a bind)
- Detects cvars modified from default value
- Removes invalid/deprecated cvars and unused scripts

#### Requires
- Java runtime > 1.8.0_40 - Download [latest Java]
- Simple Build Tool - Download [SBT]

### Installation
1. Install [SBT]
2. Download the [zip of this repository]
3. Extract zip some place
4. Open command prompt in the extracted directory containing build.sbt
 * (Hint: Shift + Right Click on the folder extracted on Windows and choose Open command window here)
5. Type `sbt assembly` to generate the .jar file in `target\scala-2.11`
6. Run the .jar file to start the application

Example output:

[Cypher's config from QC2015 original]

[Cypher's config from QC2015 cleaned]

[latest Java]: https://java.com/en/download/
[SBT]: http://www.scala-sbt.org/
[zip of this repository]: https://github.com/snappas/ScalaConfigCleaner/archive/master.zip
[Cypher's config from QC2015 original]: http://pastebin.com/Yzd4p3SS
[Cypher's config from QC2015 cleaned]: http://pastebin.com/iGC6Q3Sm