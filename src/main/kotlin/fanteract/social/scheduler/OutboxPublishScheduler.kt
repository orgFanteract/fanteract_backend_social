package fanteract.social.scheduler

import fanteract.social.client.OutboxKafkaPublisher
import fanteract.social.enumerate.OutboxStatus
import fanteract.social.repo.OutboxSocialRepo
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

// 스케줄러 2
@Component
class OutboxPublishScheduler(
    private val outboxSocialRepo: OutboxSocialRepo,
    private val publisher: OutboxKafkaPublisher,
) {
    private val workerId = "outbox-worker-${Random.nextInt(100000)}"
    private val topic = "account.mypage.delta" // 원하는 토픽명

    @Scheduled(fixedDelay = 1000)
    @Transactional
    fun publishOutbox() {
        val targets =
            outboxSocialRepo.findTop500ByOutboxStatusAndMethodNameOrderByCreatedAtAsc(
                status = OutboxStatus.NEW,
                methodName = "readMyPage",
            )

        if (targets.isEmpty()) {
            return
        }

        for (target in targets) {
            val updated = outboxSocialRepo.bulkUpdateStatus(OutboxStatus.PROCESSING, listOf(target.outboxId))

            if (updated == 0) {
                continue
            }

            try {
                publisher.publish(
                    topic = topic,
                    data = target.content,
                )

                target.outboxStatus = OutboxStatus.SENT
                outboxSocialRepo.save(target)
            } catch (ex: Exception) {
                // TODO : 재시도 로직 추가 필요
                target.outboxStatus = OutboxStatus.FAILED
                outboxSocialRepo.save(target)
            }
        }
    }
}
