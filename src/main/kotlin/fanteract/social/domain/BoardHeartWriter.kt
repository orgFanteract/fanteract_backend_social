package fanteract.social.domain

import fanteract.social.entity.BoardHeart
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
            ?: throw NoSuchElementException("조건에 맞는 게시글 좋아요 내용이 존재하지 않습니다")

        boardHeartRepo.delete(boardHeart)
    }
}