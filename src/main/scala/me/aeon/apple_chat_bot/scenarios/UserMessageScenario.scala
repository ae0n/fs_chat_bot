package me.aeon.apple_chat_bot.scenarios

import canoe.api.{Scenario, TelegramClient, chatApi, messageApi}
import canoe.models.messages.{TelegramMessage, TextMessage}
import cats.effect.{Async, Timer}
import cats.implicits._
import io.odin.Logger
import me.aeon.apple_chat_bot.services.WebsiteCache

import scala.concurrent.duration._

class UserMessageScenario[F[_]: TelegramClient: Async: Timer](itemsCache: WebsiteCache[F])(implicit log: Logger[F])
    extends BaseScenario {

  val cleanMessageTimeout: FiniteDuration = 30.seconds

  private val actions: PartialFunction[TelegramMessage, F[Option[TextMessage]]] = {
    case m: TextMessage if m.text.startsWith("/list") =>
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
    itemsCache.getCachedItems
      .map { items =>
        items.headOption
      }
      .flatMap(item => msg.chat.send(text(item.toString)))
  }

  def cleanMessage(messageOpt: Option[TextMessage]): F[Unit] = {
    messageOpt match {
      case Some(message) =>
        Timer[F].sleep(cleanMessageTimeout) >> message.delete.void
      case None =>
        ().pure[F]
    }
  }

  def scenario: Scenario[F, Unit] = {
    for {
      action <- Scenario.expect(actions)
      optMessage <- Scenario.eval(action)
      _ <- Scenario.eval(cleanMessage(optMessage))
    } yield ()
  }

}

object UserMessageScenario {

  def apply[F[_]: TelegramClient: Async: Timer: Logger](itemsCache: WebsiteCache[F]): F[UserMessageScenario[F]] = {
    new UserMessageScenario[F](itemsCache).pure[F]
  }

}
