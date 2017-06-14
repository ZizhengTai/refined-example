name := "refined-example"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.12.2"

val doobieVersion        = "0.4.1"
val refinedVersion       = "0.8.2"

libraryDependencies ++= Seq(
  "eu.timepit"     %% "refined"              % refinedVersion,
  "org.tpolecat"   %% "doobie-core-cats"     % doobieVersion,
  "org.tpolecat"   %% "doobie-postgres-cats" % doobieVersion
)
