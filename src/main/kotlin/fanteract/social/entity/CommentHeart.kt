package fanteract.social.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import fanteract.social.entity.constant.BaseEntity

@Entity
@Table(
    name = "comment_hearts",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_commentheart_user_comment",
            columnNames = ["userId", "commentId"]
        )
    ]
)
class CommentHeart (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val commentHeartId: Long =  0L,
    val userId: Long,
    val commentId: Long,
): BaseEntity()