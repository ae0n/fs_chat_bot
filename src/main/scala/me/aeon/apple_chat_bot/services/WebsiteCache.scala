package me.aeon.apple_chat_bot.services

import cats.effect
import cats.effect.Async
import me.aeon.apple_chat_bot.parser.AjWebsiteParser
import scalacache.caffeine.CaffeineCache
import scalacache.memoization
import cats.implicits._

import scala.concurrent.duration._

class WebsiteCache[F[_] : effect.Async] {

  private val parser = new AjWebsiteParser()

  private implicit val cache = CaffeineCache[Map[String, AjWebsiteParser.Item]]

  private implicit val mode = scalacache.CatsEffect.modes.async[F]

  def getCachedItems: F[Map[String, AjWebsiteParser.Item]] = memoization.memoize(Some(10.minutes)) {
    parser.collectPrices()
  }

}

object WebsiteCache {

  def apply[F[_]: Async]: F[WebsiteCache[F]] = new WebsiteCache[F].pure[F]

}
