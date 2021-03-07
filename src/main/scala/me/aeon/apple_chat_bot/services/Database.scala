package me.aeon.apple_chat_bot.services

import cats.effect.{Async, Blocker, ContextShift, Resource}
import cats.implicits.catsSyntaxApplicativeId
import doobie._
import doobie.hikari.HikariTransactor
import me.aeon.apple_chat_bot.models.DatabaseConfig

class Database[F[_]: Async: ContextShift](config: DatabaseConfig) {

  val transactor: Resource[F, HikariTransactor[F]] = {
    for {
      connectEC <- ExecutionContexts.fixedThreadPool[F](config.connectionPoolSize)
      transactionEC <- ExecutionContexts.cachedThreadPool[F]
      xa <- HikariTransactor.newHikariTransactor[F](
              config.driver,
              config.url,
              config.username,
              config.password,
              connectEC,
              Blocker.liftExecutionContext(transactionEC)
            )
      // here is a good place to add flyway migration or maybe some connection check if needed
    } yield xa
  }
}

object Database {

  def transactor[F[_]: Async: ContextShift](config: DatabaseConfig): F[Resource[F, HikariTransactor[F]]] =
    new Database[F](config).transactor.pure[F]

}
