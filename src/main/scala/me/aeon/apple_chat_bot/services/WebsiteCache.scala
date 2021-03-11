package me.aeon.apple_chat_bot.services

import cats.effect
import cats.effect.Async
import me.aeon.apple_chat_bot.parser.AjWebsiteParser
import scalacache.caffeine.CaffeineCache
import scalacache.{Mode, memoization}
import cats.implicits._

import scala.concurrent.duration._

class WebsiteCache[F[_]: effect.Async] {

  private val parser = new AjWebsiteParser()

  implicit private val cache: CaffeineCache[Map[String, AjWebsiteParser.Item]] = CaffeineCache[Map[String, AjWebsiteParser.Item]]

  implicit private val mode: Mode[F] = scalacache.CatsEffect.modes.async[F]

  def getCachedItems: F[Map[String, AjWebsiteParser.Item]] =
    memoization.memoize(Some(10.minutes)) {
      parser.collectPrices()
    }

  def getItemDescriptors: F[String] =
    getCachedItems.map { items =>
      val responseText = items.toList
        .sortBy(_._1)
        .map {
          case (k, v) =>
            s"/$k - ${v.name}"
        }
        .mkString("\n")
      responseText
    }

  def getByKey(key: String): F[Option[AjWebsiteParser.Item]] = getCachedItems.map(_.get(key))

}

object WebsiteCache {

  def apply[F[_]: Async]: F[WebsiteCache[F]] = new WebsiteCache[F].pure[F]

}
