name := "fs_chat_bot"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "org.augustjune" %% "canoe" % "0.5.1",
  "org.tpolecat" %% "doobie-core"      % Versions.doobie,
  "org.tpolecat" %% "doobie-hikari"    % Versions.doobie,
  "org.tpolecat" %% "doobie-postgres"  % Versions.doobie,
  "com.github.pureconfig" %% "pureconfig" % Versions.pureconfig,
  "com.github.pureconfig" %% "pureconfig-cats-effect" % Versions.pureconfig,
  "com.beachape" %% "enumeratum" % "1.6.1",
  "com.beachape" %% "enumeratum-doobie" % "1.6.0",
  "com.github.cb372" %% "scalacache-caffeine" % "0.28.0",
  "com.github.cb372" %% "scalacache-cats-effect" % "0.28.0",
  "net.ruippeixotog" %% "scala-scraper" % "2.2.0",
  "org.typelevel" %% "log4cats-slf4j"   % "1.2.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",

)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")