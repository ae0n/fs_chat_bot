name := "fs_chat_bot"

version := "0.1"

scalaVersion := "2.13.3"

libraryDependencies ++= Seq(
  "org.augustjune" %% "canoe" % "0.5.0-RC1",
  "org.tpolecat" %% "doobie-core"      % "0.9.0",
  "org.tpolecat" %% "doobie-hikari"    % "0.9.0",          // HikariCP transactor.
  "org.tpolecat" %% "doobie-postgres"  % "0.9.0",
)