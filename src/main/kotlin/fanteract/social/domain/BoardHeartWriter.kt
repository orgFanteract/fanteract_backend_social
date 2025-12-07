package fanteract.social.domain

import fanteract.social.entity.BoardHeart
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import fanteract.social.repo.BoardHeartRepo
import org.springframework.stereotype.Component

@Component
class BoardHeartWriter(
    private val boardHeartRepo: BoardHeartRepo,
) {
    fun create(userId: Long, boardId: Long): BoardHeart {
        val boardHeart =
            boardHeartRepo.save(
                BoardHeart(
                    userId = userId,
                    boardId = boardId,
                )
            )

        return boardHeart
    }

    fun delete(userId: Long, boardId: Long) {
        val boardHeart = boardHeartRepo.findByUserIdAndBoardId(userId, boardId)
            ?: throw ExceptionType.withType(MessageType.NOT_EXIST)

        boardHeartRepo.delete(boardHeart)
    }

    fun deleteAll(boardHeartList: List<BoardHeart>) {
        boardHeartRepo.deleteAll(boardHeartList)
    }
}