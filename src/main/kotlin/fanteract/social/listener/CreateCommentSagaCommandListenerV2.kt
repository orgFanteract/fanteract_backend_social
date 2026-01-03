package fanteract.social.listener

import fanteract.social.adapter.BoardReader
import fanteract.social.adapter.CommentWriter
import fanteract.social.adapter.MessageAdapter
import fanteract.social.dto.client.*
import fanteract.social.enumerate.EventStatus
import fanteract.social.enumerate.RiskLevel
import fanteract.social.enumerate.Status
import fanteract.social.enumerate.TopicService
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import fanteract.social.util.BaseUtil
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.Base64
import java.util.UUID

@Component
class CreateCommentSagaCommandListenerV2(
    private val messageAdapter: MessageAdapter,
    private val boardReader: BoardReader,
    private val commentWriter: CommentWriter,
) {
    private val log = KotlinLogging.logger {}
    // 1번 - 게시글 상태 검증
    @KafkaListener(topics = ["SOCIAL_SERVICE.ValidateBoardStatusCommandV2.PROCESS"], groupId = "social-service")
    fun onValidateBoardStatusCommand(message: String) {
        log.info{"onValidateBoardStatusCommand"}
        val command = BaseUtil.fromJson<EventWrapper<ValidateBoardStatusCommand>>(String(Base64.getDecoder().decode(message)))
        val (sagaId, causationId, payload) = Triple(command.sagaId, command.eventId, command.payload)

        try {
            val board = boardReader.findById(payload.boardId)
            if (board.riskLevel == RiskLevel.BLOCK || board.status == Status.DELETED) {
                throw IllegalStateException("NOT_EXIST_BOARD")
            }

            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "ValidateBoardStatusReplyV2",
                causationId = causationId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.SUCCESS,
                payload = ValidateBoardStatusReply(sagaId, causationId, true),
            )
        } catch (e: Exception) {
            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "ValidateBoardStatusReplyV2",
                causationId = causationId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.FAIL,
                payload = ValidateBoardStatusReply(sagaId, causationId, false),
            )
        }
    }

    // 3번 - 임시 댓글 생성
    @KafkaListener(topics = ["SOCIAL_SERVICE.CreateCommentPendingCommandV2.PROCESS"], groupId = "social-service")
    fun onCreateCommentPendingCommand(message: String) {
        log.info{"onCreateCommentPendingCommand"}
        val command = BaseUtil.fromJson<EventWrapper<CreateCommentCommand>>(String(Base64.getDecoder().decode(message)))
        val (sagaId, causationId, payload) = Triple(command.sagaId, command.eventId, command.payload)

        try {
            val comment =
                commentWriter.create(
                    boardId = payload.boardId,
                    userId = payload.userId,
                    content = payload.content,
                    riskLevel = RiskLevel.UNKNOWN,
                )

            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "CreateCommentReplyV2",
                causationId = causationId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.SUCCESS,
                payload =
                    CreateCommentReply(
                        sagaId = sagaId,
                        eventId = causationId,
                        success = true,
                        commentId = comment.commentId,
                        riskLevel = payload.riskLevel,
                    ),
            )
        } catch (e: Exception) {
            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "CreateCommentReplyV2",
                causationId = causationId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.FAIL,
                payload = CreateCommentReply(sagaId, causationId, false, null, null),
            )
        }
    }
}
