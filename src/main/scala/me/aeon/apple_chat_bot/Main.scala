package me.aeon.apple_chat_bot

import canoe.api.{Bot, TelegramClient}
import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import doobie.util.transactor.Transactor
import fs2._
import me.aeon.apple_chat_bot.models.AppConfig
import me.aeon.apple_chat_bot.scenarios.{CallbackHandler, UserCheckJob, UserJoinScenario}
import me.aeon.apple_chat_bot.services.{Database, UserService}
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  val blocker = Blocker.liftExecutionContext(ExecutionContext.global)

  def startBot(config: AppConfig, transactorResource: Resource[IO, Transactor[IO]]) = {
    for {
      transactor <- Stream.resource(transactorResource)
      implicit0(client: TelegramClient[IO]) <- Stream.resource(
        TelegramClient.global[IO](config.botToken)
      )
      userService <- Stream.eval(UserService[IO](transactor))
      userJoinScenario <- Stream.eval(UserJoinScenario[IO](userService))
      callbackHandler <- Stream.eval(CallbackHandler[IO](userService))
      _ <- UserCheckJob.stream(userService) concurrently Bot.polling[IO].follow(userJoinScenario.scenario).through(callbackHandler.callbacks)
    } yield ()

  }

  override def run(args: List[String]): IO[ExitCode] = {

    val appStream: Stream[IO, ExitCode] = for {
      config <- Stream.eval[IO, AppConfig](ConfigSource.default.loadF[IO, AppConfig](blocker))
      _ = println(config)
      transactorResource <- Stream.eval(Database.transactor[IO](config.database))
      _ <- startBot(config, transactorResource)
    } yield ExitCode.Success
    appStream.compile.lastOrError
  }
}
