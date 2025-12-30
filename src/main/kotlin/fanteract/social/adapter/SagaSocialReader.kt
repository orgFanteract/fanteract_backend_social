package fanteract.social.adapter

import fanteract.social.entity.SagaSocial
import fanteract.social.enumerate.EventStatus
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import fanteract.social.repo.SagaSocialRepo
import org.springframework.stereotype.Component

@Component
class SagaSocialReader(
    private val sagaSocialRepo: SagaSocialRepo,
) {
    fun findBySagaIdAndEventNameAndEventStatus(
        sagaId: String,
        eventName: String,
        eventStatus: EventStatus,
    ): SagaSocial =
        sagaSocialRepo.findBySagaIdAndEventNameAndEventStatus(sagaId, eventName, eventStatus).firstOrNull()
            ?: throw ExceptionType.withType(MessageType.NOT_EXIST)

    fun existsBySagaIdAndEventNameAndEventStatus(
        sagaId: String,
        eventName: String,
        eventStatus: EventStatus,
    ): Boolean = sagaSocialRepo.existsBySagaIdAndEventNameAndEventStatus(sagaId, eventName, eventStatus)
}
