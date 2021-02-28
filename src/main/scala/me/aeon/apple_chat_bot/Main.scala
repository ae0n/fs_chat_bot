package me.aeon.apple_chat_bot

import canoe.api.{Bot, TelegramClient}
import cats.effect.{Blocker, ExitCode, IO, IOApp, Resource}
import doobie.util.transactor.Transactor
import fs2._
import me.aeon.apple_chat_bot.models.AppConfig
import me.aeon.apple_chat_bot.scenarios.{CallbackHandler, UserCheckJob, UserJoinScenario, UserMessageScenario}
import me.aeon.apple_chat_bot.services.{Database, UserService, WebsiteCache}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  val blocker = Blocker.liftExecutionContext(ExecutionContext.global)

  def startBot(config: AppConfig, transactorResource: Resource[IO, Transactor[IO]])(implicit logger: Logger[IO]) = {
    for {
      transactor <- Stream.resource(transactorResource)
      implicit0(client: TelegramClient[IO]) <- Stream.resource(
        TelegramClient.global[IO](config.botToken)
      )
      websiteCache <- Stream.eval(WebsiteCache[IO])
      userService <- Stream.eval(UserService[IO](transactor))
      userJoinScenario <- Stream.eval(UserJoinScenario[IO](userService))
      userMessageScenario <- Stream.eval(UserMessageScenario[IO](websiteCache))
      callbackHandler <- Stream.eval(CallbackHandler[IO](userService))
      _ <- UserCheckJob.stream(userService) concurrently Bot.polling[IO].follow(userJoinScenario.scenario, userMessageScenario.scenario).through(callbackHandler.callbacks)
    } yield ()

  }

  override def run(args: List[String]): IO[ExitCode] = {

    val appStream: Stream[IO, ExitCode] = for {
      implicit0(logger: Logger[IO]) <- Stream.eval(Slf4jLogger.create[IO])
      config <- Stream.eval[IO, AppConfig](ConfigSource.default.loadF[IO, AppConfig](blocker))
      _ <- Stream.eval(logger.info(config.toString))
      transactorResource <- Stream.eval(Database.transactor[IO](config.database))
      _ <- startBot(config, transactorResource)
    } yield ExitCode.Success
    appStream.compile.lastOrError
  }
}
