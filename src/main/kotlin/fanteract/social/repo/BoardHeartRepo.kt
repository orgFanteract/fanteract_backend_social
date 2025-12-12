package fanteract.social.repo

import fanteract.social.entity.BoardHeart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BoardHeartRepo: JpaRepository<BoardHeart, Long> {
    fun findByBoardIdIn(idList: List<Long>): List<BoardHeart>
    fun findByUserIdAndBoardId(userId: Long, boardId: Long): BoardHeart?
    fun existsByUserIdAndBoardId(userId: Long, boardId: Long): Boolean

    @Modifying
    @Query("""
    delete from BoardHeart bh
    where bh.userId = :userId
      and bh.boardId = :boardId
""")
    fun deleteByUserIdAndBoardId(
        @Param("userId") userId: Long,
        @Param("boardId") boardId: Long
    ): Int

    @Modifying
    @Query("""
        delete from BoardHeart bh
        where bh.boardId = :boardId
    """)
    fun deleteByBoardId(
        @Param("boardId") boardId: Long
    )
}