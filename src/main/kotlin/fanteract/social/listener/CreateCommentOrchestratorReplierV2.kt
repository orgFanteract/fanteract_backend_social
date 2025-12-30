package fanteract.social.listener

import fanteract.social.adapter.SagaInboxWriter
import fanteract.social.dto.client.*
import fanteract.social.enumerate.SagaStatus
import fanteract.social.orchestrator.CreateCommentOrchestratorV2
import fanteract.social.orchestrator.CreateCommentSagaStep
import fanteract.social.repo.SagaInstanceRepo
import fanteract.social.util.BaseUtil
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Base64

@Transactional
@Service
class CreateCommentOrchestratorReplierV2(
    private val sagaInstanceRepo: SagaInstanceRepo,
    private val orchestrator: CreateCommentOrchestratorV2,
    private val sagaInboxWriter: SagaInboxWriter,
) {
    // 1번 - 게시글 상태 검증
    @KafkaListener(
        topics = ["SOCIAL_SERVICE.ValidateBoardStatusReplyV2.SUCCESS", "SOCIAL_SERVICE.ValidateBoardStatusReplyV2.FAIL"],
        groupId = "social-orchestrator",
    )
    fun onValidateBoardStatusReply(message: String) {
        println("onValidateBoardStatusReplyV2")
        val command = BaseUtil.fromJson<EventWrapper<ValidateBoardStatusReply>>(String(Base64.getDecoder().decode(message)))

        if (!sagaInboxWriter.acceptOnce(command.sagaId, command.eventId, "ValidateBoardStatusReplyV2")) {
            return
        }

        val saga = sagaInstanceRepo.findById(command.sagaId).orElseThrow()

        if (saga.sagaStatus != SagaStatus.RUNNING || saga.step != CreateCommentSagaStep.VALIDATE_BOARD_STATUS.name) {
            return
        }

        if (command.payload.success) {
            saga.step = CreateCommentSagaStep.UPDATE_DEBIT.name
            sagaInstanceRepo.save(saga)

            orchestrator.sendUpdateDebitCommand(saga, causationId = command.eventId)
        } else {
            saga.sagaStatus = SagaStatus.FAILED
            sagaInstanceRepo.save(saga)
        }
    }

    // 2번 - 사용자 잔액 차감
    @KafkaListener(
        topics = ["ACCOUNT_SERVICE.UpdateDebitReplyV2.SUCCESS", "ACCOUNT_SERVICE.UpdateDebitReplyV2.FAIL"],
        groupId = "social-orchestrator",
    )
    fun onDebitReply(message: String) {
        println("onDebitReplyV2")
        val command = BaseUtil.fromJson<EventWrapper<DebitBalanceReply>>(String(Base64.getDecoder().decode(message)))

        if (!sagaInboxWriter.acceptOnce(command.sagaId, command.eventId, "UpdateDebitReplyV2")) {
            return
        }

        val saga = sagaInstanceRepo.findById(command.sagaId).orElseThrow()

        if (saga.sagaStatus != SagaStatus.RUNNING || saga.step != CreateCommentSagaStep.UPDATE_DEBIT.name) {
            return
        }

        val old = BaseUtil.fromJson<CreateCommentSaga>(String(Base64.getDecoder().decode(saga.contextJson)))
        val updated = old.copy(debitedCost = command.payload.cost)

        saga.contextJson = Base64.getEncoder().encodeToString(BaseUtil.toJson(updated).toByteArray())

        if (command.payload.success) {
            saga.step = CreateCommentSagaStep.CREATE_COMMENT.name
            sagaInstanceRepo.save(saga)

            orchestrator.sendCreateCommentPendingCommand(saga, causationId = command.eventId)
        } else {
            saga.sagaStatus = SagaStatus.FAILED
            sagaInstanceRepo.save(saga)
        }
    }

    // 3번 - 임시 댓글 생성
    @KafkaListener(
        topics = ["SOCIAL_SERVICE.CreateCommentReplyV2.SUCCESS", "SOCIAL_SERVICE.CreateCommentReplyV2.FAIL"],
        groupId = "social-orchestrator",
    )
    fun onCreateCommentReply(message: String) {
        println("onCreateCommentReply")
        val command = BaseUtil.fromJson<EventWrapper<CreateCommentReply>>(String(Base64.getDecoder().decode(message)))

        if (!sagaInboxWriter.acceptOnce(command.sagaId, command.eventId, "CreateCommentReplyV2")) {
            return
        }

        val saga = sagaInstanceRepo.findById(command.sagaId).orElseThrow()

        if (saga.sagaStatus != SagaStatus.RUNNING || saga.step != CreateCommentSagaStep.CREATE_COMMENT.name) {
            return
        }

        val old = BaseUtil.fromJson<CreateCommentSaga>(String(Base64.getDecoder().decode(saga.contextJson)))
        val updated = old.copy(commentId = command.payload.commentId)
        saga.contextJson = Base64.getEncoder().encodeToString(BaseUtil.toJson(updated).toByteArray())

        if (command.payload.success) {
            saga.step = CreateCommentSagaStep.END.name
            saga.sagaStatus = SagaStatus.SUCCEEDED
            sagaInstanceRepo.save(saga)

            orchestrator.publishCommentCreatedEvent(saga, causationId = command.eventId)
        } else {
            saga.step = CreateCommentSagaStep.REFUND_BALANCE.name
            saga.sagaStatus = SagaStatus.COMPENSATING
            sagaInstanceRepo.save(saga)

            orchestrator.sendRefundCommand(saga, causationId = command.eventId)
        }
    }

    // 보상 메서드 - 사용자 잔액 반환
    @KafkaListener(
        topics = ["ACCOUNT_SERVICE.RefundBalanceReplyV2.SUCCESS", "ACCOUNT_SERVICE.RefundBalanceReplyV2.FAIL"],
        groupId = "social-orchestrator",
    )
    fun onRefundBalanceReply(message: String) {
        println("onRefundBalanceReply")
        val command = BaseUtil.fromJson<EventWrapper<RefundBalanceReply>>(String(Base64.getDecoder().decode(message)))

        if (!sagaInboxWriter.acceptOnce(command.sagaId, command.eventId, "RefundBalanceReplyV2")) {
            return
        }

        val saga = sagaInstanceRepo.findById(command.sagaId).orElseThrow()

        if (saga.sagaStatus != SagaStatus.COMPENSATING || saga.step != CreateCommentSagaStep.REFUND_BALANCE.name) {
            return
        }

        if (command.payload.success) {
            saga.sagaStatus = SagaStatus.COMPENSATED
        } else {
            saga.sagaStatus = SagaStatus.FAILED
        }
        sagaInstanceRepo.save(saga)
    }
}
