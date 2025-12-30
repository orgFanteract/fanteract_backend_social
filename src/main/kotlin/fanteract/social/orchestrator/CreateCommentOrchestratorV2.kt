package fanteract.social.orchestrator

import com.fasterxml.jackson.databind.ObjectMapper
import fanteract.social.adapter.MessageAdapter
import fanteract.social.adapter.OutboxSocialWriter
import fanteract.social.client.OutboxKafkaPublisher
import fanteract.social.dto.client.CreateCommentCommand
import fanteract.social.dto.client.DebitBalanceCommand
import fanteract.social.dto.client.MyPageDeltaEvent
import fanteract.social.dto.client.RefundBalanceCommand
import fanteract.social.dto.client.RemovePendingCommentCommand
import fanteract.social.dto.client.ValidateBoardStatusCommand
import fanteract.social.dto.client.WriteCommentForUserRequest
import fanteract.social.dto.outer.CreateCommentOuterRequest
import fanteract.social.entity.OutboxSocial
import fanteract.social.entity.SagaInbox
import fanteract.social.entity.SagaInstance
import fanteract.social.enumerate.Balance
import fanteract.social.enumerate.EventStatus
import fanteract.social.enumerate.OutboxStatus
import fanteract.social.enumerate.RiskLevel
import fanteract.social.enumerate.SagaStatus
import fanteract.social.enumerate.TopicService
import fanteract.social.enumerate.WriteStatus
import fanteract.social.repo.OutboxSocialRepo
import fanteract.social.repo.SagaInboxRepository
import fanteract.social.repo.SagaInstanceRepo
import fanteract.social.util.BaseUtil
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.util.Base64
import java.util.UUID
import kotlin.random.Random

@Service
class CreateCommentOrchestratorV2(
    private val sagaInstanceRepo: SagaInstanceRepo,
    private val sagaInboxRepository: SagaInboxRepository,
    private val messageAdapter: MessageAdapter,
    private val outboxSocialWriter: OutboxSocialWriter,
    private val objectMapper: ObjectMapper,
) {
    // 0번
    fun start(
        boardId: Long,
        userId: Long,
        req: CreateCommentOuterRequest,
    ): String {
        println("start")
        val sagaId = "SAGA-${UUID.randomUUID()}"

        val payload =
            CreateCommentSaga(
                boardId = boardId,
                userId = userId,
                content = req.content,
            )
        val contextJson = Base64.getEncoder().encodeToString(BaseUtil.toJson(payload).toByteArray())

        sagaInstanceRepo.save(
            SagaInstance(
                sagaId = sagaId,
                sagaType = "CREATE_COMMENT_V2",
                sagaStatus = SagaStatus.RUNNING,
                step = CreateCommentSagaStep.VALIDATE_BOARD_STATUS.name,
                contextJson = contextJson,
            ),
        )

        sendValidateBoardStatusCommand(sagaId, boardId)
        return sagaId
    }

    // 1번 - 게시글 상태 검증
    fun sendValidateBoardStatusCommand(
        sagaId: String,
        boardId: Long,
    ) {
        println("sendValidateBoardStatusCommand")
        messageAdapter.sendEventUsingBroker(
            sagaId = sagaId,
            eventId = "EVENT-${UUID.randomUUID()}",
            eventName = "ValidateBoardStatusCommandV2",
            causationId = null,
            topicService = TopicService.SOCIAL_SERVICE,
            eventStatus = EventStatus.PROCESS,
            payload = ValidateBoardStatusCommand(boardId),
        )
    }

    // 2번 - 사용자 잔액 차감
    fun sendUpdateDebitCommand(
        saga: SagaInstance,
        causationId: String,
    ) {
        println("sendUpdateDebitCommand")
        val content = decodeSaga(saga)

        messageAdapter.sendEventUsingBroker(
            sagaId = saga.sagaId,
            eventId = "EVENT-${UUID.randomUUID()}",
            eventName = "UpdateDebitCommandV2",
            causationId = causationId,
            topicService = TopicService.ACCOUNT_SERVICE,
            eventStatus = EventStatus.PROCESS,
            payload =
                DebitBalanceCommand(
                    boardId = content.boardId!!,
                    userId = content.userId!!,
                    content = content.content!!,
                    cost = Balance.COMMENT.cost,
                ),
        )
    }

    // 3번 - 임시 댓글 생성
    fun sendCreateCommentPendingCommand(
        saga: SagaInstance,
        causationId: String,
    ) {
        println("sendCreateCommentPendingCommand")
        val content = decodeSaga(saga)

        messageAdapter.sendEventUsingBroker(
            sagaId = saga.sagaId,
            eventId = "EVENT-${UUID.randomUUID()}",
            eventName = "CreateCommentPendingCommandV2",
            causationId = causationId,
            topicService = TopicService.SOCIAL_SERVICE,
            eventStatus = EventStatus.PROCESS,
            payload =
                CreateCommentCommand(
                    boardId = content.boardId!!,
                    userId = content.userId!!,
                    content = content.content!!,
                ),
        )
    }

    // 보상 메서드 - 사용자 잔액 반환 (account에 존재)
    fun sendRefundCommand(
        saga: SagaInstance,
        causationId: String,
    ) {
        println("sendRefundCommand")
        val content = decodeSaga(saga)

        messageAdapter.sendEventUsingBroker(
            sagaId = saga.sagaId,
            eventId = "EVENT-${UUID.randomUUID()}",
            eventName = "RefundBalanceCommandV2",
            causationId = causationId,
            topicService = TopicService.ACCOUNT_SERVICE,
            eventStatus = EventStatus.PROCESS,
            payload =
                RefundBalanceCommand(
                    userId = content.userId!!,
                    cost = content.debitedCost!!,
                ),
        )
    }

    // 사가 탈출 메서드 -> ProcessorV2
    fun publishCommentCreatedEvent(
        saga: SagaInstance,
        causationId: String,
    ) {
        println("publishCommentCreatedEvent")
        val content = decodeSaga(saga)

        // 아웃박스 적용
        val payload =
            objectMapper.writeValueAsString(
                CommentCreatedSender(
                    sagaId = saga.sagaId,
                    causationId = causationId,
                    commentId = content.commentId!!,
                    boardId = content.boardId!!,
                    userId = content.userId!!,
                    content = content.content!!,
                ),
            )

        outboxSocialWriter.create(
            topic = "SOCIAL_SERVICE.CommentCreatedEventV2.SUCCESS",
            content = payload,
            outboxStatus = OutboxStatus.NEW,
            methodName = "createComment",
        )
    }

    private fun decodeSaga(saga: SagaInstance): CreateCommentSaga {
        val decodedJson = String(Base64.getDecoder().decode(saga.contextJson))
        val command = BaseUtil.fromJson<CreateCommentSaga>(decodedJson)
        return command
    }
}

data class CommentCreatedSender(
    val sagaId: String,
    val causationId: String? = null,
    val commentId: Long,
    val boardId: Long,
    val userId: Long,
    val content: String,
)

enum class CreateCommentSagaStep {
    VALIDATE_BOARD_STATUS,
    UPDATE_DEBIT,
    CREATE_COMMENT,
    REFUND_BALANCE,
    END,
}

data class CreateCommentSaga(
    val boardId: Long? = null,
    val userId: Long? = null,
    val content: String? = null,
    var debitedCost: Int? = null,
    val commentId: Long? = null,
    val riskLevel: RiskLevel? = null,
    var addedActivePoint: Int? = null,
)
