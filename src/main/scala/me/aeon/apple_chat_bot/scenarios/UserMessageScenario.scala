package me.aeon.apple_chat_bot.scenarios

import canoe.api.{Scenario, TelegramClient, chatApi}
import canoe.models.messages.{TelegramMessage, TextMessage}
import cats.effect.Async
import cats.implicits._
import me.aeon.apple_chat_bot.services.WebsiteCache

class UserMessageScenario[F[_] : TelegramClient : Async](itemsCache: WebsiteCache[F]) extends BaseScenario {

  private val actions: PartialFunction[TelegramMessage, F[_]] = {
    case m: TextMessage if (m.text == "/list") =>
      println(m)
      itemsCache.getCachedItems.flatMap { items =>
        val responseText = items.toList.sortBy(_._1).map {
          case (k, v) =>
            s"/$k - ${v.name}"
        }.mkString("\n")
        m.chat.send(text(responseText), Some(m.messageId))
      }
  }

  def handleUserResponse(msg: TextMessage): F[_] = {
    itemsCache.getCachedItems.map { items =>
      items.headOption
    }.flatMap(item =>
      msg.chat.send(text(item.toString))
    )
  }

  def scenario: Scenario[F, Unit] = {
    for {
      action <- Scenario.expect(actions)
      _ <- Scenario.eval(action)
    } yield ()
  }

}

object UserMessageScenario {

  def apply[F[_] : TelegramClient : Async](itemsCache: WebsiteCache[F]): F[UserMessageScenario[F]] = {
    new UserMessageScenario[F](itemsCache).pure[F]
  }

}
