package fanteract.social.adapter

import fanteract.social.dto.client.MessageWrapper
import fanteract.social.entity.CommentHeart
import fanteract.social.enumerate.Status
import fanteract.social.enumerate.TopicService
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import fanteract.social.repo.CommentHeartRepo
import fanteract.social.util.BaseUtil
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.util.Base64
import kotlin.String

@Component
class CommentHeartWriter(
    private val commentHeartRepo: CommentHeartRepo,
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {

    fun create(
        userId: Long,
        commentId: Long,
        status: Status = Status.ACTIVATED
    ): CommentHeart {
        val commentHeart = CommentHeart(
            userId = userId,
            commentId = commentId,
        )

        commentHeart.status = status

        return commentHeartRepo.save(commentHeart)
    }

    fun deleteByUserIdAndCommentId(userId: Long, commentId: Long) {
        commentHeartRepo.deleteByUserIdAndCommentId(userId, commentId)
    }

    fun delete(heart: CommentHeart){
        commentHeartRepo.delete(heart)
    }

    fun deleteAll(heartList: List<CommentHeart>){
        commentHeartRepo.deleteAll(heartList)
    }

    fun updateStatus(commentHeart: CommentHeart, status: Status) {
        commentHeart.status = status
        commentHeartRepo.save(commentHeart)
    }

    fun deleteByCommentIdIn(idList: List<Long>) {
        commentHeartRepo.deleteByCommentIdIn(idList)
    }

    fun deleteByCommentId(commentId: Long) {
        commentHeartRepo.deleteByCommentId(commentId)
    }
}