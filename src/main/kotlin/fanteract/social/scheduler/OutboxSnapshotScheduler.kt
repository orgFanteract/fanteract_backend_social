package fanteract.social.scheduler

import com.fasterxml.jackson.databind.ObjectMapper
import fanteract.social.dto.client.MyPageDeltaEvent
import fanteract.social.entity.OutboxSocial
import fanteract.social.enumerate.OutboxStatus
import fanteract.social.repo.OutboxSocialRepo
import fanteract.social.util.DeltaInMemoryStorage
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class OutboxSnapshotScheduler(
    private val deltaStorage: DeltaInMemoryStorage,
    private val outboxSocialRepo: OutboxSocialRepo,
    private val objectMapper: ObjectMapper,
) {
    @Scheduled(fixedDelay = 10000)
    fun flushToOutbox() {
        val snapshot = deltaStorage.snapshot()

        snapshot.forEach { (userId, fields) ->
            val payload = objectMapper.writeValueAsString(
                MyPageDeltaEvent(userId = userId, deltas = fields)
            )

            // 아웃 박스 저장
            outboxSocialRepo.save(
                OutboxSocial(
                    content = payload,
                    topic = "account.mypagtarget.delta",
                    outboxStatus = OutboxStatus.NEW,
                    methodName = "readMyPage"
                )
            )

            // 저장 성공 시 차감
            fields.forEach { (field, delta) ->
                deltaStorage.subtract(userId, field, delta)
            }
        }
    }
}