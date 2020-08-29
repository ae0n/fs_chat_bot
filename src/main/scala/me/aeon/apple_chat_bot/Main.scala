package me.aeon.apple_chat_bot

import canoe.api.{Bot, TelegramClient}
import cats.effect.{Blocker, ExitCode, IO, IOApp}
import fs2._
import me.aeon.apple_chat_bot.models.AppConfig
import pureconfig.ConfigSource
import pureconfig.module.catseffect.syntax._
import cats.implicits._

import scala.concurrent.ExecutionContext

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] = {
    implicit val contextShift = IO.contextShift(ExecutionContext.global)
    val blocker = Blocker.liftExecutionContext(ExecutionContext.global)

    val appStream: Stream[IO, ExitCode] = for {
      config <- Stream.eval[IO, AppConfig](ConfigSource.default.loadF[IO, AppConfig](blocker))
      _ = println(config)
      implicit0(client: TelegramClient[IO]) <- Stream.resource(
        TelegramClient.global[IO](config.botToken)
      )
      _ <- Bot.polling[IO].follow( ??? )

    } yield ExitCode.Success
    appStream.compile.lastOrError
  }
}
