package fanteract.social.adapter

import fanteract.social.dto.client.EventWrapper
import fanteract.social.dto.client.MessageWrapper
import fanteract.social.enumerate.EventStatus
import fanteract.social.enumerate.TopicService
import fanteract.social.util.BaseUtil
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.Base64

@Component
class MessageAdapter(
    private val kafkaTemplate: KafkaTemplate<String, String>,
    private val sagaSocialWriter: SagaSocialWriter,
) {
    fun <T> sendMessageUsingBroker(
        message: T,
        topicService: TopicService,
        methodName: String,
    ) {
        val content =
            MessageWrapper(
                methodName = methodName,
                content = message,
            )

        val baseContent = Base64.getEncoder().encodeToString(BaseUtil.toJson(content).toByteArray())

        kafkaTemplate.send(
            "$topicService.$methodName",
            baseContent
        )
    }

    fun <T> sendEventUsingBroker(
        sagaId: String,
        eventId: String,
        eventName: String,
        causationId: String?,
        topicService: TopicService,
        eventStatus: EventStatus = EventStatus.PROCESS,
        payload: T
    ) {
        val content =
            EventWrapper(
                sagaId = sagaId,
                eventId = eventId,
                eventName = eventName,
                causationId = causationId,
                eventStatus = eventStatus,
                payload = payload,
            )

        val baseContent = Base64.getEncoder().encodeToString(BaseUtil.toJson(content).toByteArray())

        kafkaTemplate.send(
            "$topicService.$eventName.$eventStatus",
            baseContent
        )

    }
}