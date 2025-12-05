package fanteract.social.domain

import fanteract.social.entity.CommentHeart
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
                ?: throw NoSuchElementException("조건에 맞는 코멘트 좋아요 내용이 존재하지 않습니다")

        commentHeartRepo.delete(commentHeart)
    }
}