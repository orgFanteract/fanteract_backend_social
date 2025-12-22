package fanteract.social.repo

import fanteract.social.entity.SagaInbox
import org.springframework.data.jpa.repository.JpaRepository

interface SagaInboxRepository : JpaRepository<SagaInbox, Long> {
    fun existsBySagaIdAndEventId(sagaId: String, eventId: String): Boolean
}