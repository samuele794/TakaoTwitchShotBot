package botscript

import botscript.Costant.logger
import botscript.Costant.twitchBaseUrl
import botscript.client.StreamersClient
import botscript.client.TakaoTwitchClient
import botscript.dto.Streamer
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import kotlin.system.exitProcess

val streamerClient = StreamersClient()

val takaoTwitchClient = TakaoTwitchClient()

lateinit var streamersActive: List<Streamer>

fun main() {
	runBlocking {
		takaoTwitchClient.initConfig()
		takaoTwitchClient.initClient()

		streamersActive = streamerClient.getStreamers().filterNot {
			it == takaoTwitchClient.channelName
		}.map {
			Streamer(it)
		}

		takaoTwitchClient.twitchClient.chat.joinChannel(takaoTwitchClient.channelName)

		takaoTwitchClient.twitchClient.eventManager.onEvent(ChannelMessageEvent::class.java) {
			streamersActive
				.find { streamer -> streamer.username == it.user.name }
				?.let { streamer ->
					if (streamer.cdSoTime == null || streamer.cdSoTime?.isBefore(Instant.now()) == true) {
						streamer.cdSoTime = Instant.now().plus(takaoTwitchClient.durationCooldownSo)
						val userResult =
							takaoTwitchClient.twitchClient.helix.getChannelInformation(null, listOf(it.user.id))
								.execute()

						it.publishSOMessage(it.user, userResult.channels.first().gameName, takaoTwitchClient.soMessage)
					}
				}
		}

		launch {
			while (true) {
				when (readLine() ?: "") {
					"online" -> {
						val streamsResult =
							takaoTwitchClient.twitchClient.helix.getStreams(null, null, null, null, null, null, null,
								streamersActive.map {
									it.username
								}
							).execute()

						AsciiTable().apply {
							addRule()
							addRow("Username", "Categoria", "View Counter", "Link")
							addRule()

							streamsResult.streams.forEach {
								addRow(it.userName, it.gameName, it.viewerCount, twitchBaseUrl + it.userName)
							}
							addRule()
							setPadding(2)

							setTextAlignment(TextAlignment.CENTER)
							println(render(200))
						}
					}
					"exit" -> {
						exitProcess(0)
					}
					"info" -> {
						AsciiTable().apply {
							addRule()
							addRow("Username", "Orario per nuova notifica")
							addRule()

							streamersActive.forEach {
								addRow(
									it.username,
									if (it.cdSoTime != null) DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL)
										.withLocale(Locale.getDefault())
										.withZone(ZoneId.systemDefault())
										.format(it.cdSoTime)
									else
										it.cdSoTime.toString()
								)
							}
							addRule()
							setPadding(2)

							setTextAlignment(TextAlignment.CENTER)
							println(render(100))
						}
					}
					else -> {
						logger.info("Comando non valido")
					}
				}
			}
		}
	}
}