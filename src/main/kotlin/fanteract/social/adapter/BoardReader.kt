package fanteract.social.adapter

import fanteract.social.entity.Board
import fanteract.social.enumerate.RiskLevel
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import fanteract.social.repo.BoardRepo
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class BoardReader(
    private val boardRepo: BoardRepo,
) {
    fun readByUserId(
        pageable: Pageable,
        userId: Long,
    ): Page<Board> = boardRepo.findByUserId(userId, pageable)

    fun findAllExceptBlock(pageable: Pageable): Page<Board> = boardRepo.findAllByRiskLevelNot(RiskLevel.BLOCK, pageable)

    fun findById(boardId: Long): Board = boardRepo.findById(boardId).orElseThrow { ExceptionType.withType(MessageType.NOT_EXIST) }

    fun existsById(boardId: Long): Boolean = boardRepo.existsById(boardId)

    fun countByUserId(userId: Long): Long = boardRepo.countByUserId(userId)

    fun countByUserIdAndRiskLevel(
        userId: Long,
        riskLevel: RiskLevel,
    ): Long = boardRepo.countByUserIdAndRiskLevel(userId, riskLevel)

    fun findByUserIdAndRiskLevel(
        userId: Long,
        riskLevel: RiskLevel,
        pageable: PageRequest,
    ): Page<Board> = boardRepo.findByUserAndRiskLevel(userId, riskLevel, pageable)
}
