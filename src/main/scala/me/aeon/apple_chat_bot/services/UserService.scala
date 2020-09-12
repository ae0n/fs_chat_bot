package me.aeon.apple_chat_bot.services

import cats.effect.{Resource, Sync}
import cats.implicits._
import doobie._
import doobie.implicits._
import me.aeon.apple_chat_bot.dao.ChatUsersDao
import me.aeon.apple_chat_bot.models.ChatUser

class UserService[F[_]: Sync](transactorResource: Resource[F, Transactor[F]]) {

  def getUserById(id: Int): F[Option[ChatUser]] = {
    transactorResource.use { xa =>
      ChatUsersDao.findById(id).transact(xa)
    }
  }

  def addUser(user: ChatUser) = {
    transactorResource.use { xa =>
      ChatUsersDao.insert(user).transact(xa)
    }
  }

  def getOrCreateUser(user: ChatUser) = {
    getUserById(user.id).flatMap(_.fold(addUser(user))(_.some.pure[F]))
  }

}

object UserService {

  def apply[F[_]: Sync](transactorResource: Resource[F, Transactor[F]]) = {
    (new UserService[F](transactorResource)).pure[F]
  }
}
