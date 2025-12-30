package fanteract.social.adapter

import fanteract.social.entity.Comment
import fanteract.social.enumerate.RiskLevel
import fanteract.social.enumerate.Status
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import fanteract.social.repo.CommentRepo
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CommentReader(
    private val commentRepo: CommentRepo,
) {
    fun findByBoardIdIn(idList: List<Long>): List<Comment> = commentRepo.findByBoardIdIn(idList)

    fun findByBoardId(
        boardId: Long,
        pageable: Pageable,
    ): Page<Comment> = commentRepo.findByBoardId(boardId, pageable)

    fun findByBoardId(boardId: Long): List<Comment> = commentRepo.findByBoardId(boardId)

    fun findByUserId(
        userId: Long,
        pageable: Pageable,
    ): Page<Comment> = commentRepo.findByUserId(userId, pageable)

    fun findById(commentId: Long): Comment {
        val comment =
            commentRepo
                .findById(commentId)
                .orElseThrow { ExceptionType.withType(MessageType.NOT_EXIST) }

        if (comment.status == Status.DELETED) {
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        return comment
    }

    fun existsById(commentId: Long): Boolean = commentRepo.existsById(commentId)

    fun countByUserId(userId: Long): Long = commentRepo.countByUserId(userId)

    fun countByUserIdAndRiskLevel(
        userId: Long,
        riskLevel: RiskLevel,
    ): Long = commentRepo.countByUserIdAndRiskLevel(userId, riskLevel)

    fun findByUserIdAndRiskLevel(
        userId: Long,
        riskLevel: RiskLevel,
        pageable: PageRequest,
    ): Page<Comment> = commentRepo.findByUserAndRiskLevel(userId, riskLevel, pageable)

    fun findIncompleteOrStuckComments(stuckBefore: LocalDateTime): List<Comment> = commentRepo.findIncompleteOrStuckComments(stuckBefore)
}
