package me.aeon.apple_chat_bot.scenarios

import canoe.api._
import canoe.models.User
import canoe.models.messages.{ChatMemberAdded, TelegramMessage, TextMessage}
import cats.data.OptionT
import cats.effect.Async
import cats.implicits._
import me.aeon.apple_chat_bot.helpers.Permissions
import me.aeon.apple_chat_bot.models.{ChatUser, UserState}
import me.aeon.apple_chat_bot.services.UserService

class UserJoinScenario[F[_]: TelegramClient: Async](userService: UserService[F]) extends BaseScenario {

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
        case Some(u) if u.status == UserState.Blocked =>
          msg.chat.kickUser(user.id) >>
            msg.chat.send(text(strings.alreadyBannedMessage), msg.messageId.some)
        case Some(_) =>
          msg.chat.send(text(strings.welcomeBackMessage), msg.messageId.some)
        case None =>
          val newUser = ChatUser.fromUser(user, msg.chat).copy(status = UserState.Unchecked)
          for {
            _ <- OptionT(userService.addUser(newUser)).getOrElseF(Async[F].raiseError(new Throwable("unable to create user")))
            _ <- msg.chat.restrictMember(user.id, Permissions.restrictedPermissions)
            message <-
              msg.chat.send(text(strings.userJoinMessage), msg.messageId.some, button(strings.imNotABot, user.id.toString))
          } yield message
      }
  }

  def scenario(implicit FA: Async[F]): Scenario[F, Unit] = {
    for {
      newMembers <- Scenario.expect(memberAddedPF)
      _ <- Scenario.eval(sendGreetingMessage(newMembers))
    } yield ()
  }

}

object UserJoinScenario {

  def apply[F[_]: TelegramClient: Async](userService: UserService[F]): F[UserJoinScenario[F]] = {
    new UserJoinScenario[F](userService).pure[F]
  }

}
