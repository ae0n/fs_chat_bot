package me.aeon.apple_chat_bot.scenarios

import canoe.api.{Scenario, _}
import canoe.models.User
import canoe.models.messages.{ChatMemberAdded, TelegramMessage, TextMessage}
import cats.data.OptionT
import cats.effect.Async
import cats.implicits._
import io.odin.Logger
import me.aeon.apple_chat_bot.helpers.Permissions
import me.aeon.apple_chat_bot.models.{ChatUser, UserState}
import me.aeon.apple_chat_bot.services.UserService

class UserJoinScenario[F[_]: TelegramClient: Async: Logger](userService: UserService[F]) extends BaseScenario {

  private val memberAddedPF: PartialFunction[TelegramMessage, ChatMemberAdded] = {
    case m: ChatMemberAdded => m
  }

  def sendGreetingMessage(msg: ChatMemberAdded): F[Unit] = {
    msg.newChatMembers.toList.traverse(greetUser(msg, _)).void
  }

  def greetUser(msg: ChatMemberAdded, user: User): F[TextMessage] = {
    userService
      .getUserById(user.id)
      .flatMap {
        case Some(_) =>
          msg.chat.send(text(strings.welcomeBackMessage), msg.messageId.some)
        case None =>
          val newUser = ChatUser.fromUser(user, msg.chat).copy(status = UserState.Unchecked)
          for {
            _ <- OptionT(userService.addUser(newUser)).getOrElseF(Async[F].raiseError(new Throwable("unable to create user")))
            message <-
              msg.chat.send(text(strings.userJoinMessage), msg.messageId.some, button(strings.imNotABot, user.id.toString))
          } yield message
      }
  }

  def scenario(implicit FA: Async[F]): Scenario[F, Unit] = {
    (for {
      newMembers <- Scenario.expect(memberAddedPF)
      _ <- Scenario.eval(sendGreetingMessage(newMembers))
    } yield ()).handleErrorWith { err =>
      Scenario.eval {
        Logger[F].error("Got error in UserJoinScenario", err)
      }
    }
  }

}

object UserJoinScenario {

  def apply[F[_]: TelegramClient: Async: Logger](userService: UserService[F]): F[UserJoinScenario[F]] = {
    new UserJoinScenario[F](userService).pure[F]
  }

}
