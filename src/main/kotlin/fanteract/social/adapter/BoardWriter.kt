package fanteract.social.adapter

import fanteract.social.entity.Board
import fanteract.social.enumerate.RiskLevel
import fanteract.social.enumerate.Status
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
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
        val board =
            boardRepo.save(
                Board(
                    title = title,
                    content = content,
                    userId = userId,
                    riskLevel = riskLevel,
                ),
            )

        return board
    }

    fun update(
        boardId: Long,
        title: String,
        content: String,
    ) {
        val preBoard = boardRepo.findById(boardId).orElseThrow { ExceptionType.withType(MessageType.NOT_EXIST) }

        preBoard.title = title
        preBoard.content = content

        boardRepo.save(preBoard)
    }

    fun delete(board: Board) {
        board.status = Status.DELETED

        boardRepo.save(board)
    }
}
