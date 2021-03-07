package me.aeon.apple_chat_bot.scenarios

import canoe.api.{Scenario, TelegramClient, chatApi}
import canoe.models.messages.{TelegramMessage, TextMessage}
import cats.effect.{Async, Timer}
import cats.implicits._
import io.odin.Logger
import me.aeon.apple_chat_bot.services.WebsiteCache

class UserMessageScenario[F[_] : TelegramClient : Async](itemsCache: WebsiteCache[F])(implicit log: Logger[F]) extends BaseScenario {

class UserMessageScenario[F[_] : TelegramClient : Async : Timer](itemsCache: WebsiteCache[F])(implicit log: Logger[F]) extends BaseScenario {

  private val actions: PartialFunction[TelegramMessage, F[Option[TextMessage]]] = {
    case m: TextMessage if m.text == "/list" =>
      log.info(m.toString) >>
        itemsCache.getItemDescriptors.flatMap { responseText =>
          m.chat.send(text(responseText), Some(m.messageId)).map(_.some)
        }
    case m: TextMessage if m.text.startsWith("/") =>
      itemsCache.getByKey(m.text.drop(1)).flatMap {
        case Some(item) =>
          m.chat.send(text(item.toString), Some(m.messageId)).map(_.some)
        case None => Option.empty[TextMessage].pure[F]
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

  def apply[F[_] : TelegramClient : Async : Timer : Logger](itemsCache: WebsiteCache[F]): F[UserMessageScenario[F]] = {
    new UserMessageScenario[F](itemsCache).pure[F]
  }

}
