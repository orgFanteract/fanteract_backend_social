package fanteract.social.adapter

import fanteract.social.entity.BoardHeart
import fanteract.social.repo.BoardHeartRepo
import org.springframework.stereotype.Component

@Component
class BoardHeartReader(
    private val boardHeartRepo: BoardHeartRepo,
) {
    fun findByBoardIdIn(idList: List<Long>): List<BoardHeart> = boardHeartRepo.findByBoardIdIn(idList)

    fun existsByUserIdAndBoardId(
        userId: Long,
        boardId: Long,
    ): Boolean = boardHeartRepo.existsByUserIdAndBoardId(userId, boardId)
}
