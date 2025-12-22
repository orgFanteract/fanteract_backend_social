package fanteract.social.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import fanteract.social.entity.constant.BaseEntity
import fanteract.social.enumerate.RiskLevel
import jakarta.persistence.Index
import java.time.LocalDateTime

@Entity
@Table(
    name = "comments",
    indexes = [
        Index(
            name = "idx_comments_user_status",
            columnList = "user_id, status"
        ),
        Index(
            name = "idx_comments_user_risk_status",
            columnList = "user_id, risk_level, status"
        ),
        // 재처리 스캔용
        Index(
            name = "idx_comments_post_process",
            columnList = "is_completed, post_processing, post_processed_at"
        ),
    ]
)
class Comment (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val commentId: Long = 0L,
    var content: String,
    val boardId: Long,
    val userId: Long,
    @Enumerated(EnumType.STRING)
    var riskLevel: RiskLevel = RiskLevel.UNKNOWN,

    // idempotency
    var isFiltered: Boolean = false,
    var isActivePointApplied: Boolean = false,
    var isAlarmToBoardUserSent: Boolean = false,
    var isAlarmToCommentUsersSent: Boolean = false,
    var isCompleted: Boolean = false,

    // post-processing
    var postProcessing: Boolean = false, // 후처리 진행 여부
    var postProcessedAt: LocalDateTime? = null, // 마지막 후처리 시간
    var postProcessRetryCount: Int = 0, // 후처리 재시도 횟수
    var postProcessLastError: String? = null, // 마지막 실패 원인
): BaseEntity()