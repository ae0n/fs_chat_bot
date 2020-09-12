package me.aeon.apple_chat_bot.scenarios

import canoe.api._
import canoe.api.models.Keyboard
import canoe.models.messages.{ChatMemberAdded, TelegramMessage, TextMessage}
import canoe.models.outgoing.TextContent
import canoe.models.{InlineKeyboardButton, InlineKeyboardMarkup, User}
import cats.effect.Async
import cats.implicits._
import me.aeon.apple_chat_bot.models.{ChatUser, UserState}
import me.aeon.apple_chat_bot.services.UserService

class UserJoinScenario[F[_]: TelegramClient: Async](userService: UserService[F]) {

  private val memberAddedPF: PartialFunction[TelegramMessage, ChatMemberAdded] = {
    case m: ChatMemberAdded => m
  }

  def text(str: String): TextContent = TextContent(str)

  def sendGreetingMessage(msg: ChatMemberAdded): F[Unit] = {
    msg.newChatMembers.toList.traverse(greetUser(msg, _)).void
  }

  def greetUser(msg: ChatMemberAdded, user: User): F[TextMessage] = {
    val keyboardButton = InlineKeyboardButton.callbackData(strings.imNotABot, "")
    val markup = InlineKeyboardMarkup.singleButton(keyboardButton)

    userService
      .getUserById(user.id)
      .flatMap {
        case Some(u) if u.state == UserState.Blocked =>
          msg.chat.kickUser(user.id) >>
            msg.chat.send(text(strings.alreadyBannedMessage), msg.messageId.some)
        case Some(_) =>
          msg.chat.send(text(strings.welcomeBackMessage), msg.messageId.some)
        case None =>
          val newUser = ChatUser.fromUser(user).copy(state = UserState.Unchecked)
          userService.addUser(newUser) >>
            msg.chat.send(text(strings.userJoinMessage), msg.messageId.some, Keyboard.Inline(markup))
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
    (new UserJoinScenario[F](userService)).pure[F]
  }

}
