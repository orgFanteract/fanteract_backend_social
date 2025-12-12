package fanteract.social.consumer

import fanteract.social.adapter.AlarmWriter
import fanteract.social.dto.client.CreateAlarmRequest
import fanteract.social.dto.client.MessageWrapper
import fanteract.social.util.BaseUtil
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.Base64
import kotlin.String

@Component
class EventConsumer(
    private val alarmWriter: AlarmWriter,
) {
    @KafkaListener(
        topics = ["SOCIAL_SERVICE.createAlarm"],
        groupId = "social-service"
    )
    fun consumeCreateChat(message: String){
        println("consumeCreateChat exec")
        println(message)
        val decodedJson = String(Base64.getDecoder().decode(message))

        val response = BaseUtil.fromJson<MessageWrapper<CreateAlarmRequest>>(decodedJson)
        println(response)

        alarmWriter.create(
            userId = response.content.userId,
            targetUserId = response.content.targetUserId,
            contentType = response.content.contentType,
            contentId = response.content.contentId,
            alarmStatus = response.content.alarmStatus,
        )
    }
}