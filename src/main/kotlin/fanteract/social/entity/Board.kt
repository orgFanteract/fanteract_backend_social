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

@Entity
@Table(name = "boards")
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