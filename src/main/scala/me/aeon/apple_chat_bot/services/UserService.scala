package me.aeon.apple_chat_bot.services

import cats.effect.Sync
import cats.implicits._
import doobie._
import doobie.implicits._
import io.odin.Logger
import me.aeon.apple_chat_bot.dao.ChatUsersDao
import me.aeon.apple_chat_bot.models.{ChatUser, UserState}

import java.time.LocalDateTime

class UserService[F[_] : Sync](transactor: Transactor[F])(implicit log: Logger[F]) {

  def getUserById(id: Int): F[Option[ChatUser]] = {
    ChatUsersDao.findById(id).transact(transactor).recoverWith {
      e => log.error("Unable to get user by id", e).map(_ => None)
    }
  }

  def addUser(user: ChatUser): F[Option[ChatUser]] = {
    Sync[F].handleErrorWith(ChatUsersDao.insert(user).transact(transactor))(e =>
      log.error("addUser error", e).map(_ => None)
    )
  }

  def getOrCreateUser(user: ChatUser): F[Option[ChatUser]] = {
    getUserById(user.id).flatMap(_.fold(addUser(user))(_.some.pure[F]))
  }

  def updateUserState(userId: Int, state: UserState): F[Int] = {
    ChatUsersDao.updateUserState(userId, state).transact(transactor).recoverWith{
      e => log.error(s"Unable to update user $userId state", e).map(_ => 0)
    }
  }

  def findUsersWithState(state: UserState): F[Seq[ChatUser]] = {
    ChatUsersDao.findByStatus(state).transact(transactor).recoverWith{
      e => log.error(s"Unable to users with state: $state", e).map(_ => Seq.empty[ChatUser])
    }
  }

  def findUsersWhoMissedCheckingTime(): F[Seq[ChatUser]] = {
    findUsersWithState(UserState.Unchecked)
      .map(_.filter { u =>
        u.lastStatusChangedAt.isBefore(LocalDateTime.now().minusMinutes(3))
      })
  }

}

object UserService {

  def apply[F[_] : Sync : Logger](transactor: Transactor[F]): F[UserService[F]] = {
    new UserService[F](transactor).pure[F]
  }
}
