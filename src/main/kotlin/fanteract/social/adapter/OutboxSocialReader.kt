package fanteract.social.adapter

import fanteract.social.entity.OutboxSocial
import fanteract.social.enumerate.OutboxStatus
import fanteract.social.repo.OutboxSocialRepo
import org.springframework.stereotype.Component

@Component
class OutboxSocialReader(
    private val outboxSocialRepo: OutboxSocialRepo,
) {
    fun findTop500ByOutboxStatusAndMethodNameOrderByCreatedAtAsc(
        outboxStatus: OutboxStatus,
        methodName: String,
    ): List<OutboxSocial> = outboxSocialRepo.findTop500ByOutboxStatusAndMethodNameOrderByCreatedAtAsc(outboxStatus, methodName)

    fun findAllByOutboxStatusAndMethodNameOrderByCreatedAtDesc(
        outboxStatus: OutboxStatus,
        methodName: String,
    ): List<OutboxSocial> = outboxSocialRepo.findAllByOutboxStatusAndMethodNameOrderByCreatedAtDesc(outboxStatus, methodName)
}
