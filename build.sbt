name := "fs_chat_bot"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "org.augustjune" %% "canoe" % "0.5.1",
  "org.tpolecat" %% "doobie-core"      % Versions.doobie,
  "org.tpolecat" %% "doobie-hikari"    % Versions.doobie,
  "org.tpolecat" %% "doobie-postgres"  % Versions.doobie,
  "org.flywaydb" % "flyway-core" % "7.0.0",
  "com.github.pureconfig" %% "pureconfig" % Versions.pureconfig,
  "com.github.pureconfig" %% "pureconfig-cats-effect" % Versions.pureconfig,
  "com.beachape" %% "enumeratum" % "1.6.1",
  "com.beachape" %% "enumeratum-doobie" % "1.6.0",
  "com.github.cb372" %% "scalacache-caffeine" % "0.28.0",
  "com.github.cb372" %% "scalacache-cats-effect" % "0.28.0",
  "net.ruippeixotog" %% "scala-scraper" % "2.2.0",
  "com.github.valskalla" %% "odin-core" % "0.11.0",
  "com.github.valskalla" %% "odin-extras" % "0.11.0",

)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")

enablePlugins(JavaAppPackaging, DockerPlugin)
dockerBaseImage := "openjdk:14"
dockerRepository := Some("ghcr.io")
dockerUsername := Some("ae0n")
dockerUpdateLatest := true