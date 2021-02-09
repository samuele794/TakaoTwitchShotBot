package botscript.dto


import com.fasterxml.jackson.annotation.JsonProperty

data class ConfigurationDTO(
	@JsonProperty("applicationId")
	val applicationId: String,
	@JsonProperty("applicationSecret")
	val applicationSecret: String,
	@JsonProperty("oAuth2ChatKey")
	val oAuth2ChatKey: String,
	@JsonProperty("soMessage")
	val soMessage: String,
	@JsonProperty("streamerName")
	val streamerName: String
)