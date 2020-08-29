package me.aeon.apple_chat_bot

import canoe.api.{Bot, TelegramClient}
import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import cats.syntax.functor._

object Main extends IOApp {
  override def run(args: List[String]): IO[ExitCode] = {
    Stream
      .resource(
        TelegramClient
          .global[IO]("")
      )
      .flatMap { implicit client =>
        Bot.polling[IO].follow()
      }
      .compile
      .drain.as(ExitCode.Success)
  }
}
