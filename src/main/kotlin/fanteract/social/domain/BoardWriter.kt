package fanteract.social.domain

import fanteract.social.entity.Board
import fanteract.social.enumerate.RiskLevel
import fanteract.social.repo.BoardRepo
import org.springframework.stereotype.Component

@Component
class BoardWriter(
    private val boardRepo: BoardRepo,
) {
    fun create(
        title: String,
        content: String,
        userId: Long,
        riskLevel: RiskLevel,
    ): Board {
        val board = boardRepo.save(
            Board(
                title = title,
                content = content,
                userId = userId,
                riskLevel = riskLevel,
            )
        )

        return board
    }

    fun update(boardId: Long, title: String, content: String) {
        val preBoard = boardRepo.findById(boardId).orElseThrow{NoSuchElementException("조건에 맞는 게시글이 존재하지 않습니다")}

        preBoard.title = title
        preBoard.content = content

        boardRepo.save(preBoard)
    }
}