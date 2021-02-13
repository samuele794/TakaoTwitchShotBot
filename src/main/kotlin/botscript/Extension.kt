package botscript

import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.common.events.domain.EventUser

fun ChannelMessageEvent.publishSOMessage(user: EventUser, gameName: String, message: String) {
	twitchChat.sendMessage(
		channel.name,
		message.format(user.name, gameName, Costant.twitchBaseUrl + user.name) + Costant.takaoRefTag
	)
}