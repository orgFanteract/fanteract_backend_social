package fanteract.social.domain

import fanteract.social.entity.CommentHeart
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import fanteract.social.repo.CommentHeartRepo
import org.springframework.stereotype.Component

@Component
class CommentHeartWriter(
    private val commentHeartRepo: CommentHeartRepo,
) {

    fun create(userId: Long, commentId: Long): CommentHeart {
        val commentHeart =
            commentHeartRepo.save(
                CommentHeart(
                    userId = userId,
                    commentId = commentId,
                )
            )

        return commentHeart
    }

    fun delete(userId: Long, commentId: Long) {
        val commentHeart =
            commentHeartRepo.findByUserIdAndCommentId(userId, commentId)
                ?: throw ExceptionType.withType(MessageType.NOT_EXIST)

        commentHeartRepo.delete(commentHeart)
    }
}