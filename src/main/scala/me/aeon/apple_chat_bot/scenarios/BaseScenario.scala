package me.aeon.apple_chat_bot.scenarios

import canoe.api.models.Keyboard
import canoe.models.{InlineKeyboardButton, InlineKeyboardMarkup}
import canoe.models.outgoing.TextContent

trait BaseScenario {

  def text(str: String): TextContent = TextContent(str)

  def button(text: String, callback: String): Keyboard.Inline = {
    val keyboardButton = InlineKeyboardButton.callbackData(text, callback)
    Keyboard.Inline(InlineKeyboardMarkup.singleButton(keyboardButton))
  }


}
