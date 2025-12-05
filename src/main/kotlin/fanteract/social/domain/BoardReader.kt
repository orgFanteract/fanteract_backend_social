package fanteract.social.domain

import fanteract.social.entity.Board
import fanteract.social.enumerate.RiskLevel
import fanteract.social.repo.BoardRepo
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class BoardReader(
    private val boardRepo: BoardRepo,
) {
    fun readByUserId(pageable: Pageable, userId: Long): Page<Board> {
        return boardRepo.findByUserId(userId, pageable)
    }

    fun findAllExceptBlock(
        pageable: Pageable
    ): Page<Board> {
        return boardRepo.findAllByRiskLevelNot(RiskLevel.BLOCK, pageable)
    }

    fun findById(boardId: Long): Board {
        return boardRepo.findById(boardId).orElseThrow{NoSuchElementException("조건에 맞는 게시글이 존재하지 않습니다")}
    }

    fun existsById(boardId: Long): Boolean {
        return boardRepo.existsById(boardId)
    }

    fun countByUserId(userId: Long): Long {
        return boardRepo.countByUserId(userId)
    }

    fun countByUserIdAndRiskLevel(userId: Long, riskLevel: RiskLevel): Long {
        return boardRepo.countByUserIdAndRiskLevel(userId, riskLevel)
    }

    fun findByUserIdAndRiskLevel(userId: Long, riskLevel: RiskLevel, pageable: PageRequest): Page<Board> {
        return boardRepo.findByUserAndRiskLevel(userId, riskLevel, pageable)
    }
}