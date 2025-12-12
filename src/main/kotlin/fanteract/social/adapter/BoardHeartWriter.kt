package fanteract.social.adapter

import fanteract.social.entity.BoardHeart
import fanteract.social.enumerate.Status
import fanteract.social.repo.BoardHeartRepo
import org.springframework.stereotype.Component

@Component
class BoardHeartWriter(
    private val boardHeartRepo: BoardHeartRepo,
) {
    fun create(userId: Long, boardId: Long, status: Status = Status.ACTIVATED): BoardHeart {
        val boardHeart =
            boardHeartRepo.save(
                BoardHeart(
                    userId = userId,
                    boardId = boardId,
                )
            )
        boardHeart.status = status

        return boardHeart
    }

    fun deleteByUserIdAndBoardId(userId: Long, boardId: Long) {
        boardHeartRepo.deleteByUserIdAndBoardId(userId, boardId)
    }

    fun deleteAll(boardHeartList: List<BoardHeart>) {
        boardHeartRepo.deleteAll(boardHeartList)
    }

    fun delete(boardHeart: BoardHeart) {
        boardHeartRepo.delete(boardHeart)
    }

    fun updateStatus(boardHeart: BoardHeart, status: Status) {
        boardHeart.status = status
        boardHeartRepo.save(boardHeart)
    }

    fun deleteByBoardId(boardId: Long) {
        boardHeartRepo.deleteByBoardId(boardId)
    }
}