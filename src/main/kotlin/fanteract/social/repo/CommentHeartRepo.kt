package fanteract.social.repo

import fanteract.social.entity.CommentHeart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CommentHeartRepo: JpaRepository<CommentHeart, Long> {
    fun findByCommentIdIn(idList: List<Long>): List<CommentHeart>
    fun findByUserIdAndCommentId(userId: Long, commentId: Long): CommentHeart?
    fun existsByUserIdAndCommentId(userId: Long, commentId: Long): Boolean
}