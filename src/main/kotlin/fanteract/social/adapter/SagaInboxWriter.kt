package fanteract.social.adapter

import fanteract.social.entity.SagaInbox
import fanteract.social.repo.SagaInboxRepository
import jakarta.transaction.Transactional
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Component

@Component
class SagaInboxWriter(
    private val sagaInboxRepository: SagaInboxRepository,
) {
    // 여기서 유니크 중복 발생한 경험 있음 -> 카프카 재전송 때문인듯
    @Transactional
    fun acceptOnce(
        sagaId: String,
        eventId: String,
        eventName: String,
    ): Boolean =
        try {
            sagaInboxRepository.save(SagaInbox(sagaId = sagaId, eventId = eventId, eventName = eventName))
            sagaInboxRepository.flush()
            true
        } catch (e: DataIntegrityViolationException) {
            false
        }
}
