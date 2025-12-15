package fanteract.social.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
class BaseUtil2 {
    companion object {

        @PublishedApi
        internal val objectMapper: ObjectMapper =
            ObjectMapper().registerKotlinModule()

        inline fun <reified T : Any> fromJsonGeneric(json: String): T {
            try {
                return objectMapper.readValue(
                    json,
                    object : TypeReference<T>() {}
                )
            } catch (e: JsonProcessingException) {
                throw RuntimeException("제이슨 파싱 실패", e)
            }
        }

        fun toJson(any: Any): String =
            objectMapper.writeValueAsString(any)
    }
}