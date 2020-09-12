package me.aeon.apple_chat_bot.dao

import doobie._
import doobie.implicits._
import doobie.util.meta.LegacyLocalDateMetaInstance
import me.aeon.apple_chat_bot.models.ChatUser

object ChatUsersDao extends LegacyLocalDateMetaInstance {

  private def findByBase(fragment: Fragment) = {
    (sql"""SELECT id, first_name, last_name, username, status, first_visit FROM users WHERE""" ++ fragment)
      .query[ChatUser]
  }

  private def findByOne(fragment: Fragment) = {
    findByBase(fragment).option
  }

  private def findByMany(fragment: Fragment) = {
    findByBase(fragment).to[Seq]
  }

  def insert(user: ChatUser): doobie.ConnectionIO[Option[ChatUser]] = {
    for {
      _ <- sql"""INSERT INTO users(id, first_name, last_name, username, status, first_visit)
           |VALUES(${user.id}, ${user.firstName}, ${user.lastName}, ${user.username}, ${user.state}, ${user.firstVisit})""".stripMargin.update.run
      id <- sql"select lastval()".query[Int].unique
      newUser <- findById(id)
    } yield newUser
  }

  def findById(id: Int): doobie.ConnectionIO[Option[ChatUser]] = findByOne(fr"""id=$id""")

  def findByStatus(status: String): doobie.ConnectionIO[Seq[ChatUser]] = findByMany(fr"""status=$status""")

}
