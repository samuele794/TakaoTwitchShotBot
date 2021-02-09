package botscript.dto


import com.fasterxml.jackson.annotation.JsonProperty

data class StreamerDTO(
	@JsonProperty("communityStreamers")
	val communityStreamers: List<String>
)