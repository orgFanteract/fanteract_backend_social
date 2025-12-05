package fanteract.social.dto.outer

import fanteract.social.enumerate.AlarmStatus
import fanteract.social.enumerate.ContentType
import fanteract.social.enumerate.RiskLevel
import java.time.LocalDateTime

data class ReadAlarmListOuterResponse(
    val contents: List<ReadAlarmOuterResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class ReadAlarmOuterResponse(
    val alarmId: Long,
    val userId: Long,
    val targetUserId: Long,
    val contentId: Long,
    val contentType: ContentType,
    val alarmStatus: AlarmStatus,
)

data class CreateBoardOuterResponse(
    val boardId: Long?,
    val riskLevel: RiskLevel,
)

data class ReadBoardListOuterResponse(
    val contents: List<ReadBoardDetailOuterResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class ReadBoardDetailOuterResponse(
    val boardId: Long,
    val userName: String,
    val title: String,
    val content: String,
    val commentCount: Int,
    val heartCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class CreateCommentOuterResponse(
    val commentId: Long?,
    val riskLevel: RiskLevel,
)

data class CreateHeartInCommentOuterResponse(
    val commentHeartId: Long,
)

data class ReadCommentPageOuterResponse(
    val contents: List<ReadCommentResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class ReadCommentResponse(
    val commentId: Long,
    val boardId: Long,
    val content: String,
    val heartCount: Int,
    val userName: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class FilterResult(
    val action: RiskLevel,
    val reason: String? = null,
    val score: Double? = null, // ML 토크시티 점수 등
)

data class CreateHeartInBoardOuterResponse(
    val boardHeartId: Long,
)
