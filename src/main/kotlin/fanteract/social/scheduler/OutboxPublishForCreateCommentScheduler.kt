package fanteract.social.scheduler

import com.fasterxml.jackson.databind.ObjectMapper
import fanteract.social.adapter.MessageAdapter
import fanteract.social.client.OutboxKafkaPublisher
import fanteract.social.dto.client.EventWrapper
import fanteract.social.enumerate.EventStatus
import fanteract.social.enumerate.OutboxStatus
import fanteract.social.enumerate.TopicService
import fanteract.social.orchestrator.CommentCreatedEvent
import fanteract.social.orchestrator.CommentCreatedSender
import fanteract.social.repo.OutboxSocialRepo
import fanteract.social.util.BaseUtil
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

// 스케줄러 2
@Component
class OutboxPublishForCreateCommentScheduler(
    private val outboxSocialRepo: OutboxSocialRepo,
    private val publisher: OutboxKafkaPublisher,
    private val messageAdapter: MessageAdapter,
    private val objectMapper: ObjectMapper,
) {
    @Scheduled(fixedDelay = 1000)
    @Transactional
    fun publishOutbox() {
        println("publishOutbox - createComment")
        val targets =
            outboxSocialRepo.findTop500ByOutboxStatusAndMethodNameOrderByCreatedAtAsc(
                status = OutboxStatus.NEW,
                methodName = "createComment"
            )

        if (targets.isEmpty())
            return

        for (target in targets) {
            val updated =
                outboxSocialRepo.bulkUpdateStatus(
                    status = OutboxStatus.PROCESSING,
                    ids = listOf(target.outboxId)
                )

            if (updated == 0)
                continue

            try {
                val sender =
                    objectMapper.readValue(target.content, CommentCreatedSender::class.java)

                val eventPayload =
                    CommentCreatedEvent(
                        commentId = sender.commentId,
                        boardId = sender.boardId,
                        userId = sender.userId,
                        content = sender.content
                    )

                messageAdapter.sendEventUsingBroker(
                    sagaId = sender.sagaId,
                    eventId = "EVENT-${UUID.randomUUID()}",
                    eventName = "CommentCreatedEventV2",
                    causationId = sender.causationId,
                    topicService = TopicService.SOCIAL_SERVICE,
                    eventStatus = EventStatus.SUCCESS,
                    payload = eventPayload,
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