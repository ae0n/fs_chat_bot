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
)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")