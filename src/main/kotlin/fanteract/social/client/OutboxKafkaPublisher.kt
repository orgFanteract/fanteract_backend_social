package fanteract.social.client

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class OutboxKafkaPublisher(
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    fun publish(topic: String, data: String) {
        kafkaTemplate.send(topic, data)
    }
}