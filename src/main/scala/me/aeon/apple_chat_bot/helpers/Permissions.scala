package me.aeon.apple_chat_bot.helpers

import canoe.models.ChatPermissions
import cats.implicits._

object Permissions {

  val defaultPermissions = ChatPermissions(
    canSendMessages = true.some,
    canSendMediaMessages = true.some,
    canSendPolls = true.some,
    canSendOtherMessages = true.some,
    canAddWebPagePreviews = true.some
  )

  val restrictedPermissions = ChatPermissions(
    canSendMessages = false.some,
    canSendMediaMessages = false.some,
    canSendPolls = false.some,
    canSendOtherMessages = false.some,
    canAddWebPagePreviews = false.some
  )

}
