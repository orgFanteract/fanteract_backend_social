package fanteract.social.adapter

import fanteract.social.entity.SagaSocial
import fanteract.social.enumerate.EventStatus
import fanteract.social.repo.SagaSocialRepo
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.stereotype.Component

@Component
class SagaSocialWriter(
    private val sagaSocialRepo: SagaSocialRepo,
) {
    fun create(
        sagaId: String,
        eventId: String,
        eventName: String,
        payload: String?,
        eventStatus: EventStatus,
        isExec: Boolean = true,
    ) {
        val sagaSocial =
            sagaSocialRepo.save(
                SagaSocial(
                    sagaId = sagaId,
                    eventId = eventId,
                    eventName = eventName,
                    payload = payload,
                    eventStatus = eventStatus,
                    isExec = isExec,
                ),
            )
    }
}
