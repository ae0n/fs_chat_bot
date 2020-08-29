package me.aeon.apple_chat_bot.models
import pureconfig.generic.semiauto._


case class AppConfig(botToken: String)

object AppConfig {
  implicit val appConfigReader = deriveReader[AppConfig]
}
