package fanteract.social.repo

import fanteract.social.entity.Comment
import fanteract.social.enumerate.RiskLevel
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CommentRepo : JpaRepository<Comment, Long> {

    @Query("""
        SELECT c
        FROM Comment c
        WHERE c.boardId = :boardId
          AND c.status = 'ACTIVATED'
          AND c.riskLevel <> 'BLOCK'
    """)
    fun findByBoardId(
        @Param("boardId") boardId: Long,
        pageable: Pageable
    ): Page<Comment>

    @Query("""
        SELECT c
        FROM Comment c
        WHERE c.boardId = :boardId
          AND c.status = 'ACTIVATED'
          AND c.riskLevel <> 'BLOCK'
    """)
    fun findByBoardId(
        @Param("boardId") boardId: Long,
    ): List<Comment>

    @Query("""
        SELECT c
        FROM Comment c
        WHERE c.userId = :userId
          AND c.status = 'ACTIVATED'
          AND c.riskLevel <> 'BLOCK'
    """)
    fun findByUserId(
        @Param("userId") userId: Long,
        pageable: Pageable
    ): Page<Comment>

    @Query("""
        SELECT c
        FROM Comment c
        WHERE c.boardId IN :idList
          AND c.status = 'ACTIVATED'
          AND c.riskLevel <> 'BLOCK'
    """)
    fun findByBoardIdIn(
        @Param("idList") idList: List<Long>
    ): List<Comment>

    @Query("""
        SELECT COUNT(c)
        FROM Comment c
        WHERE c.userId = :userId
          AND c.status = 'ACTIVATED'
    """)
    fun countByUserId(@Param("userId") userId: Long): Long


    @Query("""
    SELECT COUNT(c)
    FROM Comment c
    WHERE c.userId = :userId
      AND c.riskLevel = :riskLevel
      AND c.status = 'ACTIVATED'
""")
    fun countByUserIdAndRiskLevel(
        @Param("userId") userId: Long,
        @Param("riskLevel") riskLevel: RiskLevel
    ): Long

    @Query("""
        SELECT c
        FROM Comment c
        WHERE c.userId = :userId
          AND c.riskLevel = :riskLevel
          AND c.status = 'ACTIVATED'
    """)
    fun findByUserAndRiskLevel(
        @Param("userId") userId: Long,
        @Param("riskLevel") riskLevel: RiskLevel,
        pageable: PageRequest
    ): Page<Comment>
}