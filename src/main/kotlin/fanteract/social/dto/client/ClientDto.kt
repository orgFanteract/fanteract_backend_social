package fanteract.social.dto.client

import com.fasterxml.jackson.databind.JsonNode
import fanteract.social.enumerate.EventStatus
import fanteract.social.enumerate.RiskLevel
import java.time.Instant
import java.util.UUID

data class MessageWrapper<T>(
    val methodName: String,
    val content: T
)

data class EventWrapper<T>(
    val sagaId: String,
    val eventId: String,
    val eventName: String,
    val causationId: String? = null,
    val occurredAt: String = Instant.now().toString(),
    val eventStatus: EventStatus,
    val payload: T
)

data class EventWrapperForLog(
    val sagaId: String,
    val eventId: String,
    val eventName: String,
    val causationId: String? = null,
    val occurredAt: String = Instant.now().toString(),
    val eventStatus: EventStatus,
    val payload: JsonNode?
)

data class DebitIfEnoughEventDto(
    val userId: Long,
    val boardId: Long,
    val content: String,
)

data class ValidateBoardStatusDto(
    val boardId: Long,
    val userId: Long,
    val content: String,
)

data class FilterCommentContentEventDto(
    val userId: Long,
    val boardId: Long,
    val content: String,
    val cost: Int,
)

data class CreateCommentEventDto(
    val boardId: Long,
    val userId: Long,
    val content: String,
    val riskLevel: RiskLevel,
    val cost: Int,
)

data class UpdateActivePointEventDto(
    val boardId: Long,
    val userId: Long,
    val commentId: Long,
    val cost: Int,
)


data class CreateAlarmToBoardUserEventDto(
    val userId: Long,
    val boardId: Long,
    val commentId: Long,
    val cost: Int,
    val activePoint: Int,
)

data class CreateAlarmToOtherCommentUserEventDto(
    val userId: Long,
    val contentId: Long,
    val cost: Int,
    val activePoint: Int,
    val commentId: Long,
)

data class CreateCommentEventCompensateDto(
    val userId: Long?,
    val refundCost: Int?,
    val refundActivePoint: Int?,
    val commentId: Long?
)

data class MyPageDeltaEvent(
    val eventId: String = UUID.randomUUID().toString(),
    val userId: Long,
    val deltas: Map<String, Long>, // 예: boardCount:+1, commentCount:+2 ...
    val createdAtEpochMs: Long = System.currentTimeMillis(),
)

// command & reply
data class ValidateBoardStatusCommand(val boardId: Long)
data class ValidateBoardStatusReply(val sagaId: String, val eventId: String, val success: Boolean)

data class DebitBalanceCommand(val boardId: Long, val userId: Long, val content: String, val cost: Int)
data class DebitBalanceReply(val sagaId: String, val eventId: String, val success: Boolean, val cost: Int?)

data class FilterContentCommand(val boardId: Long, val userId: Long, val content: String)
data class FilterContentReply(val sagaId: String, val eventId: String, val success: Boolean, val riskLevel: RiskLevel?)

data class CreateCommentCommand(val boardId: Long, val userId: Long, val content: String, val riskLevel: RiskLevel = RiskLevel.UNKNOWN)
data class CreateCommentReply(val sagaId: String, val eventId: String, val success: Boolean, val commentId: Long?, val riskLevel: RiskLevel?)

data class UpdateActivePointCommand(val userId: Long, val point: Int)
data class UpdateActivePointReply(val sagaId: String, val eventId: String, val success: Boolean, val point: Int)

data class CreateAlarmsToCommentUserCommand(val boardId: Long, val userId: Long)
data class CreateAlarmsToCommentUserReply(val sagaId: String, val eventId: String, val success: Boolean)

data class CreateAlarmsToBoardUserCommand(val boardId: Long, val userId: Long)
data class CreateAlarmsToBoardUserReply(val sagaId: String, val eventId: String, val success: Boolean)

data class CreateAlarmsCommand(val boardId: Long, val userId: Long, val commentId: Long)
data class CreateAlarmsReply(val sagaId: String, val eventId: String, val success: Boolean)

data class UpdateMyPageCounterCommand(val userId: Long, val type: String, val delta: Int, val restrictedDelta: Int)
data class UpdateMyPageCounterReply(val sagaId: String, val eventId: String, val success: Boolean)

data class RefundBalanceCommand(val userId: Long, val cost: Int)
data class RefundBalanceReply(val sagaId: String, val eventId: String, val success: Boolean)

data class RemovePendingCommentCommand(val userId: Long, val cost: Int)
data class RemovePendingCommentReply(val sagaId: String, val eventId: String, val success: Boolean)

data class RollbackActivePointCommand(val userId: Long, val point: Int)
data class RollbackActivePointReply(val sagaId: String, val eventId: String, val success: Boolean)

data class DeleteCommentCommand(val commentId: Long)
data class DeleteCommentReply(val sagaId: String, val eventId: String, val success: Boolean)

data class CreateCommentSaga(
    val boardId: Long? = null,
    val userId: Long? = null,
    val content: String? = null,
    var debitedCost: Int? = null,
    val commentId: Long? = null,
    val riskLevel: RiskLevel? = null,
    var addedActivePoint: Int? = null,
)