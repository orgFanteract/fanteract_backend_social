package fanteract.social.adapter

import fanteract.social.entity.CommentHeart
import fanteract.social.repo.CommentHeartRepo
import org.springframework.stereotype.Component

@Component
class CommentHeartReader(
    private val commentHeartRepo: CommentHeartRepo,
) {
    fun findByCommentIdIn(idList: List<Long>): List<CommentHeart> {
        return commentHeartRepo.findByCommentIdIn(idList)
    }

    fun existsByUserIdAndCommentId(userId: Long, commentId: Long): Boolean {
        return commentHeartRepo.existsByUserIdAndCommentId(userId, commentId)
    }

    fun existsByUserIdAndBoardId(userId: Long, commentId: Long) {}
}