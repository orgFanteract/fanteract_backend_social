package fanteract.social.repo

import fanteract.social.entity.Board
import fanteract.social.enumerate.RiskLevel
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface BoardRepo : JpaRepository<Board, Long> {
    @Query("""
        SELECT b FROM Board b
        WHERE b.userId = :userId
        AND b.status = 'ACTIVATED'
        AND b.riskLevel <> 'BLOCK'
    """)
    fun findByUserId(
        @Param("userId") userId: Long,
        pageable: Pageable
    ): Page<Board>


    @Query("""
        SELECT COUNT(b)
        FROM Board b
        WHERE b.userId = :userId
        AND b.status = 'ACTIVATED'
    """)
    fun countByUserId(
        @Param("userId") userId: Long
    ): Long

    @Query("""
        SELECT COUNT(b)
        FROM Board b
        WHERE b.userId = :userId
          AND b.riskLevel = :riskLevel
          AND b.status = 'ACTIVATED'
    """)
    fun countByUserIdAndRiskLevel(
        @Param("userId") userId: Long,
        @Param("riskLevel") riskLevel: RiskLevel
    ): Long

    @Query("""
        SELECT b
        FROM Board b
        WHERE b.userId = :userId
          AND b.riskLevel = :riskLevel
          AND b.status = 'ACTIVATED'
    """)
    fun findByUserAndRiskLevel(
        @Param("userId") userId: Long,
        @Param("riskLevel") riskLevel: RiskLevel,
        pageable: PageRequest
    ): Page<Board>

    fun findAllByRiskLevelNot(
        riskLevel: RiskLevel,
        pageable: Pageable
    ): Page<Board>
}