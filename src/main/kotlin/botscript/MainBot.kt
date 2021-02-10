package botscript

import botscript.dto.ConfigurationDTO
import botscript.dto.StreamerDTO
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import com.github.twitch4j.chat.events.channel.ChannelMessageEvent
import com.github.twitch4j.common.events.domain.EventUser
import de.vandermeer.asciitable.AsciiTable
import de.vandermeer.skb.interfaces.transformers.textformat.TextAlignment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.system.exitProcess
import kotlin.text.Charsets.UTF_8


lateinit var twitchClient: TwitchClient

//https://twitch4j.github.io/docs/getting-started/

//https://dev.twitch.tv/docs/api/reference#get-streams

//Link generazione token https://twitchtokengenerator.com/
lateinit var oAuth2ChatKey: String
lateinit var channelName: String

lateinit var clientId: String
lateinit var clientSecret: String
lateinit var soMessage: String

data class Streamer(val username: String, var cdSoTime: Instant? = null)

var communityStreamer: MutableList<Streamer> = arrayListOf()

lateinit var streamers: List<Streamer>

fun main() {
	jacksonObjectMapper()
		.readValue(
			StreamerDTO::class.java.classLoader.getResource("streamers.json").readText(),
			StreamerDTO::class.java
		).communityStreamers.forEach {
			communityStreamer.add(Streamer(it))
		}

	val configFile = File("./config.json")

	val configJSON = if (configFile.exists()) {
		configFile.readText()
	} else {
		ConfigurationDTO::class.java.classLoader.getResource("config.json").readText()
	}

	jacksonObjectMapper()
		.readValue(
			configJSON,
			ConfigurationDTO::class.java
		).let {
			oAuth2ChatKey = it.oAuth2ChatKey
			channelName = it.streamerName
			clientId = it.applicationId
			clientSecret = it.applicationSecret
			soMessage = it.soMessage
		}

	streamers = communityStreamer.filterNot {
		it.username == channelName
	}

	val oAuthCredential = OAuth2Credential("twitch", base64Decoder.decode(oAuth2ChatKey).toString(UTF_8))

	twitchClient = TwitchClientBuilder.builder()
		.withDefaultAuthToken(oAuthCredential)
		.withEnableChat(true)
		.withChatAccount(oAuthCredential)
		.withEnablePubSub(true)
		.withEnableHelix(true)
		.withClientId(base64Decoder.decode(clientId).toString(UTF_8))
		.withClientSecret(base64Decoder.decode(clientSecret).toString(UTF_8))
		.build()

	twitchClient.chat.joinChannel(channelName)

	twitchClient.eventManager.onEvent(ChannelMessageEvent::class.java) {
		streamers
			.find { streamer -> streamer.username == it.user.name }
			?.let { streamer ->
				if (streamer.cdSoTime == null || streamer.cdSoTime?.isBefore(Instant.now()) == true) {
					streamer.cdSoTime = Instant.now().plus(1, ChronoUnit.HOURS)
					val userResult = twitchClient.helix.getChannelInformation(null, listOf(it.user.id)).execute()

					it.publishSOMessage(it.user, userResult.channels.first().gameName)
				}
			}
	}



	GlobalScope.launch {
		while (true) {
			when (readLine() ?: "") {
				"online" -> {
					val streamsResult = twitchClient.helix.getStreams(null, null, null, null, null, null, null,
						streamers.map {
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

						streamers.forEach {
							addRow(
								it.username,
								if (it.cdSoTime != null) DateTimeFormatter.ISO_INSTANT.format(it.cdSoTime) else it.cdSoTime.toString()
							)
						}
						addRule()
						setPadding(2)

						setTextAlignment(TextAlignment.CENTER)
						println(render(50))
					}
				}
				else -> {
					logger.info("Comando non valido")
				}
			}
		}
	}

}

val logger: Logger = LoggerFactory.getLogger("TakaoBotServices")

val mapper = jsonMapper {
	addModule(kotlinModule())
	addModule(JavaTimeModule())
}

private const val takaoRefTag = " (by TakaoTwitchShotBot)"

private val base64Decoder = Base64.getDecoder()
const val twitchBaseUrl = "https://www.twitch.tv/"

fun ChannelMessageEvent.publishSOMessage(user: EventUser, gameName: String) {
	twitchChat.sendMessage(
		channel.name,
		soMessage.format(user.name, gameName, twitchBaseUrl + user.name) + takaoRefTag
	)
}