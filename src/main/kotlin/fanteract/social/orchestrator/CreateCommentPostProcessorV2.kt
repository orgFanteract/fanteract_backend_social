package fanteract.social.orchestrator

import fanteract.social.adapter.*
import fanteract.social.filter.ProfanityFilterService
import fanteract.social.dto.client.*
import fanteract.social.entity.Comment
import fanteract.social.enumerate.ActivePoint
import fanteract.social.enumerate.AlarmStatus
import fanteract.social.enumerate.ContentType
import fanteract.social.enumerate.RiskLevel
import fanteract.social.enumerate.Status
import fanteract.social.enumerate.TopicService
import fanteract.social.util.BaseUtil
import fanteract.social.util.DeltaInMemoryStorage
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.Base64

@Component
class CreateCommentPostProcessorV2(
    private val profanityFilterService: ProfanityFilterService,
    private val commentReader: CommentReader,
    private val commentWriter: CommentWriter,
    private val boardReader: BoardReader,
    private val messageAdapter: MessageAdapter,
    private val deltaInMemoryStorage: DeltaInMemoryStorage,
) {
    @KafkaListener(
        topics = ["SOCIAL_SERVICE.CommentCreatedEventV2.SUCCESS"],
        groupId = "social-postprocessor"
    )
    fun onCommentCreatedEvent(message: String) {
        println("onCommentCreatedEvent")
//        val event = BaseUtil.fromJson<EventWrapper<CommentCreatedEvent>>(
//            String(Base64.getDecoder().decode(message))
//        )
//        val payload = event.payload
        val decoded = String(Base64.getDecoder().decode(message))
        val payload = BaseUtil.fromJson<EventWrapper<CommentCreatedEvent>>(decoded).payload

        // 락 확인 및 설정
        if (commentWriter.tryAcquirePostProcess(payload.commentId) == 0) {
            return
        }

        try {
            processOneComment(payload.commentId) // 실제 진행
            commentWriter.releasePostProcessSuccess(payload.commentId) // 락 해제
        } catch (e: Exception) {
            commentWriter.releasePostProcessFail(payload.commentId, e.message ?: e::class.java.simpleName)
            throw e
        }
    }

    private fun processOneComment(commentId: Long) {
        println("processOneComment")
        // 0) 현재 상태 1회 로드
        var comment = commentReader.findById(commentId)

        // 댓글 정합성 검증
        if (!comment.isFiltered) {
            val riskLevel = profanityFilterService.checkProfanityAndUpdateAbusePoint(
                userId = comment.userId,
                text = comment.content,
            )
            commentWriter.updateRiskLevel(comment, riskLevel)

            if (riskLevel == RiskLevel.BLOCK) {
                commentWriter.updateStatus(comment, Status.ACTIVATED)
                commentWriter.updateIdempotency(comment, isFiltered = true, isCompleted = true)
                addWriteCountDelta(comment, RiskLevel.BLOCK)
                return
            } else {
                commentWriter.updateIdempotency(comment, isFiltered = true)
            }

            comment = commentReader.findById(comment.commentId)
        }

        // 사용자 활동 점수 추가
        if (!comment.isActivePointApplied && comment.riskLevel != RiskLevel.BLOCK) {
            messageAdapter.sendMessageUsingBroker(
                message = UpdateActivePointRequest(
                    userId = comment.userId,
                    activePoint = ActivePoint.COMMENT.point
                ),
                topicService = TopicService.ACCOUNT_SERVICE,
                methodName = "updateActivePoint"
            )

            commentWriter.updateIdempotency(comment, isActivePointApplied = true)
            comment = commentReader.findById(comment.commentId)
        }

        // 댓글 활성화
        commentWriter.updateStatus(comment, Status.ACTIVATED)
        comment = commentReader.findById(comment.commentId)

        // 알림 1
        if (!comment.isAlarmToBoardUserSent) {
            val boardUserId = boardReader.findById(comment.boardId).userId

            messageAdapter.sendMessageUsingBroker(
                message = CreateAlarmRequest(
                    userId = comment.userId,
                    targetUserId = boardUserId,
                    contentType = ContentType.COMMENT,
                    contentId = comment.boardId,
                    alarmStatus = AlarmStatus.CREATED,
                ),
                topicService = TopicService.SOCIAL_SERVICE,
                methodName = "createAlarm"
            )

            commentWriter.updateIdempotency(comment, isAlarmToBoardUserSent = true)
            comment = commentReader.findById(comment.commentId)
        }

        // 4) 알림 2
        if (!comment.isAlarmToCommentUsersSent) {
            val targets = commentReader.findByBoardId(comment.boardId)
                .map { it.userId }
                .distinct()

            messageAdapter.sendMessageUsingBroker(
                message = CreateAlarmListRequest(
                    userId = comment.userId,
                    targetUserIdList = targets,
                    contentType = ContentType.COMMENT,
                    contentId = comment.boardId,
                    alarmStatus = AlarmStatus.CREATED,
                ),
                topicService = TopicService.SOCIAL_SERVICE,
                methodName = "createAlarmList"
            )

            commentWriter.updateIdempotency(comment, isAlarmToCommentUsersSent = true)
            comment = commentReader.findById(comment.commentId)
        }

        // 5) 완료 조건
        if (!comment.isCompleted &&
            comment.isFiltered &&
            (comment.riskLevel == RiskLevel.BLOCK || comment.isActivePointApplied) &&
            comment.isAlarmToBoardUserSent &&
            comment.isAlarmToCommentUsersSent
        ) {
            commentWriter.updateIdempotency(comment, isCompleted = true)
            addWriteCountDelta(comment, comment.riskLevel)
        }
    }

    fun addWriteCountDelta(comment: Comment, riskLevel: RiskLevel) {
        deltaInMemoryStorage.addDelta(comment.userId, "commentCount", 1)

        if (comment.riskLevel == RiskLevel.BLOCK) {
            deltaInMemoryStorage.addDelta(comment.userId, "restrictedCommentCount", 1)
        }
    }
}

data class CommentCreatedEvent(
    val commentId: Long,
    val boardId: Long,
    val userId: Long,
    val content: String,
)