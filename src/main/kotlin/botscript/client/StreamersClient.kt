package botscript.client

import botscript.Costant
import botscript.dto.StreamerDTO
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

class StreamersClient {

	private var communityStreamer: List<String>? = null

	suspend fun getStreamers(): List<String> = withContext(Dispatchers.IO) {
		if (communityStreamer != null) {
			Costant.logger.info("Recupero Lista Streamer da cache")
			return@withContext communityStreamer!!
		}

		Costant.logger.info("Inizio Recupero Streamers da remoto")

		val httpClient = HttpClient.newHttpClient()

		val requestStreamerList = HttpRequest
			.newBuilder(URI.create(streamerJSONLink))
			.timeout(Duration.ofMinutes(1))
			.GET()
			.build()

		val result = runCatching {
			httpClient.send(requestStreamerList, HttpResponse.BodyHandlers.ofString())
		}.onFailure {
			Costant.logger.error("Errore Recupero streamers remoto", it)
		}.getOrNull()

		if (result == null) {
			Costant.logger.error("Recupero Streamers da remoto fallito, passaggio a lista locale")
			communityStreamer = jacksonObjectMapper()
				.readValue(
					StreamerDTO::class.java.classLoader.getResource("streamers.json").readText(),
					StreamerDTO::class.java
				).communityStreamers
			communityStreamer!!
		} else {
			Costant.logger.info("Recupero Streamers da remoto completato con successo")
			Costant.logger.info("Streamers da remoto: ${result.body()}")
			communityStreamer =
				jacksonObjectMapper().readValue(result.body(), StreamerDTO::class.java).communityStreamers
			communityStreamer!!
		}
	}

	companion object {
		private const val streamerJSONLink =
			"https://gist.githubusercontent.com/samuele794/a22ab3864081b0275256aa9403e67a35/raw/1ecb7d198616c4d654a4892fd82d01e6d3f61e50/streamers.json"

	}
}