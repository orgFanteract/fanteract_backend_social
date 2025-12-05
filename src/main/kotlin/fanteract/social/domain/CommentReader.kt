package fanteract.social.domain

import fanteract.social.entity.Comment
import fanteract.social.enumerate.RiskLevel
import fanteract.social.enumerate.Status
import fanteract.social.repo.CommentRepo
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class CommentReader(
    private val commentRepo: CommentRepo,
) {
    fun findByBoardIdIn(idList: List<Long>): List<Comment> {
        return commentRepo.findByBoardIdIn(idList)
    }

    fun findByBoardId(boardId: Long, pageable: Pageable): Page<Comment> {
        return commentRepo.findByBoardId(boardId, pageable)
    }

    fun findByBoardId(boardId: Long): List<Comment> {
        return commentRepo.findByBoardId(boardId)
    }

    fun findByUserId(userId: Long, pageable: Pageable): Page<Comment> {
        return commentRepo.findByUserId(userId, pageable)
    }

    fun findById(commentId: Long): Comment {
        val comment = commentRepo.findById(commentId)
            .orElseThrow { NoSuchElementException("조건에 맞는 코멘트가 존재하지 않습니다") }

        if (comment.status == Status.DELETED)
            throw NoSuchElementException("조건에 맞는 코멘트가 존재하지 않습니다")

        return comment
    }

    fun existsById(commentId: Long): Boolean {
        return commentRepo.existsById(commentId)
    }

    fun countByUserId(userId: Long): Long {
        return commentRepo.countByUserId(userId)
    }

    fun countByUserIdAndRiskLevel(userId: Long, riskLevel: RiskLevel): Long {
        return commentRepo.countByUserIdAndRiskLevel(userId, riskLevel)
    }

    fun findByUserIdAndRiskLevel(userId: Long, riskLevel: RiskLevel, pageable: PageRequest): Page<Comment> {
        return commentRepo.findByUserAndRiskLevel(userId, riskLevel, pageable)
    }
}