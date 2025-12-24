package fanteract.social.adapter

import fanteract.social.entity.OutboxSocial
import fanteract.social.enumerate.OutboxStatus
import fanteract.social.repo.OutboxSocialRepo
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OutboxSocialWriter (
    private val outboxSocialRepo: OutboxSocialRepo,
) {
    fun bulkUpdateStatus(
        status: OutboxStatus,
        idList: List<Long>
    ): Int {
        return outboxSocialRepo.bulkUpdateStatus(status, idList)
    }

    fun create(
        topic: String, // 구독 대상
        content: String, // 전송 내용
        outboxStatus: OutboxStatus, // 상태
        methodName: String,
    ): OutboxSocial{
        return outboxSocialRepo.save(
            OutboxSocial(
                topic = topic,
                content = content,
                outboxStatus = outboxStatus,
                methodName = methodName,
            )
        )
    }
}