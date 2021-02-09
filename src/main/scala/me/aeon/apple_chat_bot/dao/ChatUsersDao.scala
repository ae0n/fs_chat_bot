package me.aeon.apple_chat_bot.dao

import doobie._
import doobie.implicits._
import doobie.util.meta.{MetaConstructors, TimeMetaInstances}
import me.aeon.apple_chat_bot.models.{ChatUser, UserState}


object ChatUsersDao extends MetaConstructors with TimeMetaInstances {

  private def findByBase(fragment: Fragment) = {
    (sql"""SELECT id, first_name, last_name, username, chat_id, status, first_visit, last_status_changed_at FROM users WHERE """ ++ fragment)
      .query[ChatUser]
  }

  private def findByOne(fragment: Fragment) = {
    findByBase(fragment).option.attemptSql.map {
      case Left(value) =>
        println(value)
        None
      case Right(value) =>
        value
    }
  }

  private def findByMany(fragment: Fragment) = {
    findByBase(fragment).to[Seq].attemptSql.map {
      case Left(value) =>
        println(value)
        Seq.empty
      case Right(value) =>
        value
    }
  }

  def insert(user: ChatUser): doobie.ConnectionIO[Option[ChatUser]] = {
    (for {
      _ <-
        sql"""INSERT INTO users(id, first_name, last_name, username, chat_id, status, first_visit, last_status_changed_at)
             |VALUES(${user.id}, ${user.firstName}, ${user.lastName}, ${user.username}, ${user.chatId}, ${user.status}, ${user.firstVisit}, ${user.lastStatusChangedAt})""".stripMargin.update.run
      newUser <- findById(user.id)
    } yield newUser).attemptSql.map {
      case Left(value) =>
        println(value)
        None
      case Right(value) =>
        value
    }
  }

  def findById(id: Int): doobie.ConnectionIO[Option[ChatUser]] = findByOne(fr"""id=$id""")

  def findByStatus(status: UserState): doobie.ConnectionIO[Seq[ChatUser]] = findByMany(fr"""status=$status""")

  def updateUserState(userId: Int, state: UserState) = {
    sql"""UPDATE users
         |SET status=$state, last_status_changed_at=now()
         |WHERE id=$userId
         """.stripMargin.update.run.attemptSql.map {
      case Left(value) =>
        println(value)
        0
      case Right(value) =>
        value
    }
  }

}
