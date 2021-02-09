package me.aeon.apple_chat_bot.models

import java.time.LocalDate
import canoe.models.{Chat, User}
import enumeratum.EnumEntry.Snakecase
import enumeratum._

sealed trait UserState extends EnumEntry with Snakecase

object UserState extends Enum[UserState] with DoobieEnum[UserState] {

  val values = findValues

  case object Unchecked extends UserState

  case object Blocked extends UserState

  case object Normal extends UserState

}

case class ChatUser(id: Int, firstName: String, lastName: Option[String], username: Option[String], chatId: Long, status: UserState, firstVisit: LocalDate, lastStatusChangedAt: LocalDate)

object ChatUser {

  def fromUser(u: User, chat: Chat): ChatUser = {
    ChatUser(id = u.id,
      firstName = u.firstName,
      lastName = u.lastName,
      username = u.username,
      status = UserState.Unchecked,
      firstVisit = LocalDate.now(),
      chatId = chat.id,
      lastStatusChangedAt = LocalDate.now()
    )
  }
}
