package fanteract.social.util

import fanteract.social.dto.client.EventWrapper
import java.util.Base64

inline fun <reified T : Any> messageResolver(message: String): EventWrapper<T> {
    val decodedJson = String(Base64.getDecoder().decode(message))

    val response: EventWrapper<T> =
        BaseUtil2.fromJsonGeneric(decodedJson)

    return response
}
