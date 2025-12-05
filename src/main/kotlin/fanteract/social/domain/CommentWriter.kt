package fanteract.social.domain

import fanteract.social.entity.Comment
import fanteract.social.enumerate.RiskLevel
import fanteract.social.enumerate.Status
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
            .orElseThrow { NoSuchElementException("조건에 맞는 코멘트가 존재하지 않습니다") }

        comment.content = content
        commentRepo.save(comment)
    }

    fun delete(commentId: Long) {
        val comment = commentRepo.findById(commentId)
            .orElseThrow { NoSuchElementException("조건에 맞는 코멘트가 존재하지 않습니다") }

        comment.status = Status.DELETED
        commentRepo.save(comment)
    }
}