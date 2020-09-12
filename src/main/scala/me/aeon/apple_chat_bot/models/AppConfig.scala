package me.aeon.apple_chat_bot.models

import pureconfig.generic.semiauto._

case class DatabaseConfig(username: String, password: String, url: String, connectionPoolSize: Int, driver: String)

object DatabaseConfig {
  implicit val databaseConfigReader = deriveReader[DatabaseConfig]
}

case class AppConfig(botToken: String, database: DatabaseConfig)

object AppConfig {
  implicit val appConfigReader = deriveReader[AppConfig]
}
