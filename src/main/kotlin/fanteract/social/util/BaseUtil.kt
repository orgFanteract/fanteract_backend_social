package fanteract.social.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

class BaseUtil {
    companion object {
        inline fun <reified T: Any> fromJson(json: String): T {
            try{
                return jacksonObjectMapper().readValue<T>(json)
            } catch(e: JsonProcessingException) {
                throw kotlin.RuntimeException("제이슨 파싱 실패")
            }
        }
        fun toJson(any: Any): String{
            val objectMapper = ObjectMapper()

            try{
                return objectMapper.writeValueAsString(any)
            } catch (e: JsonProcessingException) {
                throw kotlin.RuntimeException("제이슨 직렬화 실패")
            }
        }

    }
}