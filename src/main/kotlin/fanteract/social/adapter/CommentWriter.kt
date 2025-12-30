package fanteract.social.adapter

import fanteract.social.entity.Comment
import fanteract.social.enumerate.RiskLevel
import fanteract.social.enumerate.Status
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import fanteract.social.repo.CommentRepo
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Component
class CommentWriter(
    private val commentRepo: CommentRepo,
) {
    fun create(
        boardId: Long,
        userId: Long,
        content: String,
        riskLevel: RiskLevel,
        status: Status = Status.ACTIVATED,
    ): Comment {
        val comment =
            Comment(
                boardId = boardId,
                userId = userId,
                content = content,
                riskLevel = riskLevel,
            )

        comment.status = status
        return commentRepo.save(comment)
    }

    fun update(
        commentId: Long,
        content: String,
    ) {
        val comment =
            commentRepo
                .findById(commentId)
                .orElseThrow { ExceptionType.withType(MessageType.NOT_EXIST) }

        comment.content = content
        commentRepo.save(comment)
    }

    fun delete(commentId: Long) {
        val comment =
            commentRepo
                .findById(commentId)
                .orElseThrow { ExceptionType.withType(MessageType.NOT_EXIST) }

        comment.status = Status.DELETED
        commentRepo.save(comment)
    }

    fun deleteAll(commentList: List<Comment>) {
        for (comment in commentList) {
            comment.status = Status.DELETED
            commentRepo.save(comment)
        }
    }

    fun deleteByBoardId(boardId: Long) {
        commentRepo.deleteByBoardIdAndStatus(boardId)
    }

    fun deleteById(commentId: Long) {
        commentRepo.deleteById(commentId)
    }

    fun updateRiskLevel(
        comment: Comment,
        riskLevel: RiskLevel,
    ) {
        comment.riskLevel = riskLevel
        commentRepo.save(comment)
    }

    fun updateIdempotency(
        comment: Comment,
        isFiltered: Boolean = comment.isFiltered,
        isActivePointApplied: Boolean = comment.isActivePointApplied,
        isAlarmToBoardUserSent: Boolean = comment.isAlarmToBoardUserSent,
        isAlarmToCommentUsersSent: Boolean = comment.isAlarmToCommentUsersSent,
        isCompleted: Boolean = comment.isCompleted,
    ) {
        comment.isFiltered = isFiltered
        comment.isActivePointApplied = isActivePointApplied
        comment.isAlarmToBoardUserSent = isAlarmToBoardUserSent
        comment.isAlarmToCommentUsersSent = isAlarmToCommentUsersSent
        comment.isCompleted = isCompleted

        commentRepo.save(comment)
    }

    fun updateStatus(
        comment: Comment,
        status: Status,
    ) {
        comment.status = status
        commentRepo.save(comment)
    }

    @Transactional
    fun tryAcquirePostProcess(commentId: Long): Int = commentRepo.tryAcquirePostProcess(commentId)

    @Transactional
    fun releasePostProcessSuccess(commentId: Long): Int = commentRepo.releasePostProcessSuccess(commentId)

    @Transactional
    fun releasePostProcessFail(
        commentId: Long,
        error: String,
    ): Int = commentRepo.releasePostProcessFail(commentId, error)
}
