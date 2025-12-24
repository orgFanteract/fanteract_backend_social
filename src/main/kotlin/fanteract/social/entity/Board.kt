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


@Entity
@Table(
    name = "boards",
    indexes = [
        Index(
            name = "idx_boards_user_status",
            columnList = "user_id, status"
        ),
        Index(
            name = "idx_boards_user_risk_status",
            columnList = "user_id, risk_level, status"
        )
    ]
)
class Board (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val boardId: Long = 0L,
    var title: String,
    var content: String,
    val userId: Long,
    @Enumerated(EnumType.STRING)
    val riskLevel: RiskLevel,
): BaseEntity()