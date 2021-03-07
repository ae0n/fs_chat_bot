package me.aeon.apple_chat_bot.models

import pureconfig.ConfigReader
import pureconfig.generic.semiauto._

case class DatabaseConfig(username: String, password: String, url: String, connectionPoolSize: Int, driver: String)

object DatabaseConfig {
  implicit val databaseConfigReader: ConfigReader[DatabaseConfig] = deriveReader[DatabaseConfig]
}

case class AppConfig(botToken: String, database: DatabaseConfig)

object AppConfig {
  implicit val appConfigReader: ConfigReader[AppConfig] = deriveReader[AppConfig]
}
