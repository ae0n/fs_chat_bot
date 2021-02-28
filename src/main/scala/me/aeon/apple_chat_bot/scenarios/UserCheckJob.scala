package me.aeon.apple_chat_bot.scenarios

import canoe.api.TelegramClient
import canoe.methods.chats.KickChatMember
import cats.effect.{Async, Timer}
import me.aeon.apple_chat_bot.services.UserService
import fs2._
import cats.implicits.{toTraverseOps, _}
import cats.effect._
import me.aeon.apple_chat_bot.models.{ChatUser, UserState}

import java.time.{LocalDate, LocalDateTime}
import scala.concurrent.duration._
import canoe.syntax._
import org.typelevel.log4cats.Logger


class UserCheckJob[F[_] : Async : Timer](userService: UserService[F])(implicit tgClient: TelegramClient[F], log:Logger[F]) {

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
