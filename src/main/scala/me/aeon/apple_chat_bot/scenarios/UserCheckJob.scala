package me.aeon.apple_chat_bot.scenarios

import canoe.api.TelegramClient
import canoe.methods.chats.KickChatMember
import canoe.syntax._
import cats.effect.{Async, Timer}
import cats.implicits.{toTraverseOps, _}
import fs2.Stream
import io.odin.Logger
import me.aeon.apple_chat_bot.models.{ChatUser, UserState}
import me.aeon.apple_chat_bot.services.UserService

import scala.concurrent.duration._

class UserCheckJob[F[_] : Async : Timer](userService: UserService[F])(implicit tgClient: TelegramClient[F], log: Logger[F]) {

  def checkUncheckedUsers = {
    for {
      _ <- log.info("going to check for unverified users")
      users <- userService.findUsersWhoMissedCheckingTime()
      _ <- users.traverse(kickUser)
      _ <- log.info(s"done ${users.length} users found")
    } yield {
      ()
    }
  }

  def kickUser(user: ChatUser) = {
    for {
      _ <- KickChatMember(user.chatId, user.id).call
      _ <- userService.updateUserState(user.id, UserState.Blocked)
    } yield ()
  }

  def stream = Stream.awakeEvery[F](10.seconds) *> Stream.eval(checkUncheckedUsers)
}

object UserCheckJob {

  def stream[F[_] : Async : Timer : TelegramClient : Logger](userService: UserService[F]) = {
    new UserCheckJob[F](userService).stream
  }

}
