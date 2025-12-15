package fanteract.social.dto.client

import com.fasterxml.jackson.databind.JsonNode
import fanteract.social.enumerate.EventStatus
import fanteract.social.enumerate.RiskLevel
import java.time.Instant

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