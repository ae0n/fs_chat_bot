package me.aeon.apple_chat_bot.scenarios

import canoe.api.TelegramClient
import canoe.models.{CallbackButtonSelected, CallbackQuery, Update}
import cats.Applicative
import cats.data.OptionT
import cats.effect.Async
import cats.implicits._
import fs2.Pipe
import io.odin.Logger
import me.aeon.apple_chat_bot.models.{ChatUser, UserState}
import me.aeon.apple_chat_bot.services.UserService

class CallbackHandler[F[_] : Async : TelegramClient](userService: UserService[F])(implicit log:Logger[F]) {

  def handleButtonCallback(query: CallbackQuery): F[Option[ChatUser]] = {
    OptionT.fromOption[F](query.data.flatMap(_.toIntOption))
      .filter(_ == query.from.id)
      .flatMapF[ChatUser](userService.getUserById)
      .filter(_.status == UserState.Unchecked)
      .semiflatTap(u => userService.updateUserState(u.id, UserState.Normal))
      .value
  }

  def callbacks: Pipe[F, Update, Update] = {
    _.evalTap {
      case CallbackButtonSelected(updateId, callbackQuery) =>
        log.info((updateId, callbackQuery).toString) >>
        handleButtonCallback(callbackQuery).void
      case x =>
        log.info(x.toString) >>
        Applicative[F].unit
    }
  }

}

object CallbackHandler {

  def apply[F[_] : Async : TelegramClient : Logger](userService: UserService[F]): F[CallbackHandler[F]] = {
    new CallbackHandler[F](userService).pure[F]
  }

}
