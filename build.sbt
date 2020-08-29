name := "fs_chat_bot"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "org.augustjune" %% "canoe" % "0.5.0-RC1",
  "org.tpolecat" %% "doobie-core"      % Versions.doobie,
  "org.tpolecat" %% "doobie-hikari"    % Versions.doobie,
  "org.tpolecat" %% "doobie-postgres"  % Versions.doobie,
  "com.github.pureconfig" %% "pureconfig" % Versions.pureconfig,
  "com.github.pureconfig" %% "pureconfig-cats-effect" % Versions.pureconfig,
)

addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1")