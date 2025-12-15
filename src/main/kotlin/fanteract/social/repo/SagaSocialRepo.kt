package fanteract.social.repo

import fanteract.social.entity.SagaSocial
import fanteract.social.enumerate.EventStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SagaSocialRepo: JpaRepository<SagaSocial, Long> {
    fun findBySagaIdAndEventNameAndEventStatus(
        sagaId: String,
        eventName: String,
        eventStatus: EventStatus
    ): List<SagaSocial>

    fun existsBySagaIdAndEventNameAndEventStatus(sagaId: String, eventName: String, eventStatus: fanteract.social.enumerate.EventStatus): Boolean
}