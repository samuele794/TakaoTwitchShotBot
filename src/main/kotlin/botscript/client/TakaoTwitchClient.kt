package botscript.client

import botscript.Costant
import botscript.dto.ConfigurationDTO
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.philippheuer.credentialmanager.domain.OAuth2Credential
import com.github.twitch4j.TwitchClient
import com.github.twitch4j.TwitchClientBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Duration

class TakaoTwitchClient {

	lateinit var oAuth2ChatKey: String
	lateinit var channelName: String

	lateinit var clientId: String
	lateinit var clientSecret: String
	lateinit var soMessage: String

	lateinit var durationCooldownSo: Duration

	lateinit var twitchClient: TwitchClient

	suspend fun initConfig() = withContext(Dispatchers.IO) {
		Costant.logger.info("Caricamento config Twitch")

		val configFile = File("./config.json")

		val configJSON = if (configFile.exists()) {
			configFile.readText()
		} else {
			ConfigurationDTO::class.java.classLoader.getResource("config.json").readText()
		}

		Costant.logger.info("Configurazione caricata: $configJSON")

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
				durationCooldownSo = it.getTimeAsRealValue()
			}
	}


	suspend fun initClient() = withContext(Dispatchers.IO) {
		Costant.logger.info("Inizializzazione twitch client")

		val oAuthCredential =
			OAuth2Credential("twitch", Costant.base64Decoder.decode(oAuth2ChatKey).toString(Charsets.UTF_8))

		twitchClient = TwitchClientBuilder.builder()
			.withDefaultAuthToken(oAuthCredential)
			.withEnableChat(true)
			.withChatAccount(oAuthCredential)
			.withEnablePubSub(true)
			.withEnableHelix(true)
			.withClientId(Costant.base64Decoder.decode(clientId).toString(Charsets.UTF_8))
			.withClientSecret(Costant.base64Decoder.decode(clientSecret).toString(Charsets.UTF_8))
			.build()
	}
}