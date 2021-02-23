package botscript.dto

import java.time.Instant

data class Streamer(val username: String, var cdSoTime: Instant? = null)