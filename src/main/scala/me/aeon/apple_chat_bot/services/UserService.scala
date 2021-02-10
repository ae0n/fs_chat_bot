package me.aeon.apple_chat_bot.services

import cats.effect.Sync
import cats.implicits._
import doobie._
import doobie.implicits._
import me.aeon.apple_chat_bot.dao.ChatUsersDao
import me.aeon.apple_chat_bot.models.{ChatUser, UserState}

import java.time.LocalDateTime

class UserService[F[_] : Sync](transactor: Transactor[F]) {

  def getUserById(id: Int): F[Option[ChatUser]] = {
    ChatUsersDao.findById(id).transact(transactor)
  }

  def addUser(user: ChatUser) = {
    Sync[F].handleError(ChatUsersDao.insert(user).transact(transactor)) {
      case e =>
        println(e)
        None
    }
  }

  def getOrCreateUser(user: ChatUser) = {
    getUserById(user.id).flatMap(_.fold(addUser(user))(_.some.pure[F]))
  }

  def updateUserState(userId: Int, state: UserState) = {
    ChatUsersDao.updateUserState(userId, state).transact(transactor)
  }

  def findUsersWithState(state: UserState): F[Seq[ChatUser]] = {
    ChatUsersDao.findByStatus(state).transact(transactor)
  }

  def findUsersWhoMissedCheckingTime(): F[Seq[ChatUser]] = {
    findUsersWithState(UserState.Unchecked)
      .map(_.filter { u =>
        u.lastStatusChangedAt.isBefore(LocalDateTime.now().minusMinutes(3))
      })
  }

}

object UserService {

  def apply[F[_] : Sync](transactor: Transactor[F]) = {
    (new UserService[F](transactor)).pure[F]
  }
}
