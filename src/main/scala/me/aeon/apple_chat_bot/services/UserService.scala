package me.aeon.apple_chat_bot.services

import cats.effect.Sync
import cats.implicits._
import doobie._
import doobie.implicits._
import me.aeon.apple_chat_bot.dao.ChatUsersDao
import me.aeon.apple_chat_bot.models.{ChatUser, UserState}

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

  def updateUserState(userId: Int, state: UserState) = {}

}

object UserService {

  def apply[F[_] : Sync](transactor: Transactor[F]) = {
    (new UserService[F](transactor)).pure[F]
  }
}
