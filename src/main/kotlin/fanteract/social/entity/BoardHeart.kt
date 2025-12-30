package fanteract.social.entity

import fanteract.social.entity.constant.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(
    name = "board_hearts",
    uniqueConstraints = [
        jakarta.persistence.UniqueConstraint(
            name = "uk_boardheart_user_board",
            columnNames = ["userId", "boardId"],
        ),
    ],
)
class BoardHeart(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val boardHeartId: Long = 0L,
    val userId: Long,
    val boardId: Long,
) : BaseEntity()
