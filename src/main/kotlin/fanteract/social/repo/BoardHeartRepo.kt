package fanteract.social.repo

import fanteract.social.entity.BoardHeart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BoardHeartRepo: JpaRepository<BoardHeart, Long> {
    fun findByBoardIdIn(idList: List<Long>): List<BoardHeart>
    fun findByUserIdAndBoardId(userId: Long, boardId: Long): BoardHeart?
    fun existsByUserIdAndBoardId(userId: Long, boardId: Long): Boolean
}