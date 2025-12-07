package fanteract.social.domain

import fanteract.social.entity.Comment
import fanteract.social.enumerate.RiskLevel
import fanteract.social.enumerate.Status
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import fanteract.social.repo.CommentRepo
import org.springframework.stereotype.Component

@Component
class CommentWriter(
    private val commentRepo: CommentRepo
) {

    fun create(
        boardId: Long,
        userId: Long,
        content: String,
        riskLevel: RiskLevel,
    ): Comment {
        val comment = Comment(
            boardId = boardId,
            userId = userId,
            content = content,
            riskLevel = riskLevel,
        )
        return commentRepo.save(comment)
    }

    fun update(
        commentId: Long,
        content: String
    ) {
        val comment = commentRepo.findById(commentId)
            .orElseThrow { ExceptionType.withType(MessageType.NOT_EXIST) }

        comment.content = content
        commentRepo.save(comment)
    }

    fun delete(commentId: Long) {
        val comment = commentRepo.findById(commentId)
            .orElseThrow { ExceptionType.withType(MessageType.NOT_EXIST) }

        comment.status = Status.DELETED
        commentRepo.save(comment)
    }
}