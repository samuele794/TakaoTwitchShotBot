package botscript

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*

object Costant {
	val logger: Logger = LoggerFactory.getLogger("TakaoBotServices")

	val mapper = jsonMapper {
		addModule(kotlinModule())
		addModule(JavaTimeModule())
	}

	const val takaoRefTag = " (by TakaoTwitchShotBot)"

	val base64Decoder = Base64.getDecoder()
	const val twitchBaseUrl = "https://www.twitch.tv/"

}