package me.aeon.apple_chat_bot.services

import cats.effect.{Async, Blocker, ContextShift, Resource, Sync}
import cats.implicits._
import doobie._
import doobie.hikari.HikariTransactor
import io.odin.Logger
import me.aeon.apple_chat_bot.models.DatabaseConfig
import org.flywaydb.core.Flyway

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
    } yield xa
  }
}

object Database {

  def transactor[F[_]: Async: ContextShift](config: DatabaseConfig): F[Resource[F, HikariTransactor[F]]] =
    new Database[F](config).transactor.pure[F]

  def migrate[F[_]: Sync: Logger](config: DatabaseConfig): F[Unit] = {
    Logger[F].info("Start migration") >>
      Sync[F]
        .delay {
          val flyway = Flyway.configure().dataSource(config.url, config.username, config.password).load()
          flyway.migrate()
        }
        .flatMap { r =>
          Logger[F].info(s"Migration finished. Number executed: ${r.migrationsExecuted}")
        }
        .onError { err =>
            Logger[F].error("Error occured during migration", err)
        }
  }

}
