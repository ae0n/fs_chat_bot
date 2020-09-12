package me.aeon.apple_chat_bot

import canoe.api.{Bot, TelegramClient}
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import fs2._
import me.aeon.apple_chat_bot.models.AppConfig
import me.aeon.apple_chat_bot.scenarios.UserJoinScenario
import me.aeon.apple_chat_bot.services.{Database, UserService}
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  val blocker = Blocker.liftExecutionContext(ExecutionContext.global)

  override def run(args: List[String]): IO[ExitCode] = {

    val appStream: Stream[IO, ExitCode] = for {
      config <- Stream.eval[IO, AppConfig](ConfigSource.default.loadF[IO, AppConfig](blocker))
      _ = println(config)
      implicit0(client: TelegramClient[IO]) <- Stream.resource(
                                                 TelegramClient.global[IO](config.botToken)
                                               )
      transactor <- Stream.eval(Database.transactor[IO](config.database))
      userService <- Stream.eval(UserService[IO](transactor))
      userJoinScenario <- Stream.eval(UserJoinScenario[IO](userService))
      _ <- Bot.polling[IO].follow(userJoinScenario.scenario)
    } yield ExitCode.Success
    appStream.compile.lastOrError
  }
}
