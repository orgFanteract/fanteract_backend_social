package fanteract.social.dto.inner

import fanteract.social.enumerate.RiskLevel
import java.time.LocalDateTime

data class CreateAlarmInnerResponse(
    val alarmId: Long,
)

data class ReadBoardDetailInnerResponse(
    val boardId: Long,
    val userName: String,
    val title: String,
    val commentCount: Int,
    val heartCount: Int,
    val userId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class ReadBoardCountInnerResponse(
    val count: Long,
)

data class ReadBoardExistsInnerResponse(
    val isExist: Boolean,
)

data class ReadBoardPageInnerResponse(
    val contents: List<ReadBoardInnerResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class ReadBoardInnerResponse(
    val boardId: Long = 0L,
    var title: String,
    var content: String,
    val userId: Long,
    val riskLevel: RiskLevel,
)

data class ReadCommentCountInnerResponse(
    val count: Long,
)

data class ReadCommentExistsInnerResponse(
    val isExist: Boolean,
)

data class ReadCommentPageInnerResponse(
    val contents: List<ReadCommentInnerResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class ReadCommentListInnerResponse(
    val contents: List<ReadCommentInnerResponse>,
)

data class ReadCommentInnerResponse(
    val commentId: Long,
    var content: String,
    val boardId: Long,
    val userId: Long,
    val riskLevel: RiskLevel,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class ReadCommentDetailInnerResponse(
    val commentId: Long,
    val content: String,
    val heartCount: Int,
    val userName: String,
    val userId: Long,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class BoardHeartInnerResponse(
    val boardHeartId: Long,
    val userId: Long,
    val boardId: Long,
)

data class BoardHeartListInnerResponse(
    val content: List<BoardHeartInnerResponse>,
)
