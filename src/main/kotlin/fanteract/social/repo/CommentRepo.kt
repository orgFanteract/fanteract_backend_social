package fanteract.social.repo

import fanteract.social.entity.Comment
import fanteract.social.enumerate.RiskLevel
import fanteract.social.enumerate.Status
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CommentRepo : JpaRepository<Comment, Long> {
    @Query(
        """
        SELECT c
        FROM Comment c
        WHERE c.boardId = :boardId
          AND c.status = 'ACTIVATED'
          AND c.riskLevel <> 'BLOCK'
    """,
    )
    fun findByBoardId(
        @Param("boardId") boardId: Long,
        pageable: Pageable,
    ): Page<Comment>

    @Query(
        """
        SELECT c
        FROM Comment c
        WHERE c.boardId = :boardId
          AND c.status = 'ACTIVATED'
          AND c.riskLevel <> 'BLOCK'
    """,
    )
    fun findByBoardId(
        @Param("boardId") boardId: Long,
    ): List<Comment>

    @Query(
        """
        SELECT c
        FROM Comment c
        WHERE c.userId = :userId
          AND c.status = 'ACTIVATED'
          AND c.riskLevel <> 'BLOCK'
    """,
    )
    fun findByUserId(
        @Param("userId") userId: Long,
        pageable: Pageable,
    ): Page<Comment>

    @Query(
        """
        SELECT c
        FROM Comment c
        WHERE c.boardId IN :idList
          AND c.status = 'ACTIVATED'
          AND c.riskLevel <> 'BLOCK'
    """,
    )
    fun findByBoardIdIn(
        @Param("idList") idList: List<Long>,
    ): List<Comment>

    @Query(
        """
        SELECT COUNT(c)
        FROM Comment c
        WHERE c.userId = :userId
          AND c.status = 'ACTIVATED'
    """,
    )
    fun countByUserId(
        @Param("userId") userId: Long,
    ): Long

    @Query(
        """
    SELECT COUNT(c)
    FROM Comment c
    WHERE c.userId = :userId
      AND c.riskLevel = :riskLevel
      AND c.status = 'ACTIVATED'
""",
    )
    fun countByUserIdAndRiskLevel(
        @Param("userId") userId: Long,
        @Param("riskLevel") riskLevel: RiskLevel,
    ): Long

    @Query(
        """
        SELECT c
        FROM Comment c
        WHERE c.userId = :userId
          AND c.riskLevel = :riskLevel
          AND c.status = 'ACTIVATED'
    """,
    )
    fun findByUserAndRiskLevel(
        @Param("userId") userId: Long,
        @Param("riskLevel") riskLevel: RiskLevel,
        pageable: PageRequest,
    ): Page<Comment>

    @Modifying
    @Query(
        """
        update Comment c
        set c.status = :status
        where c.boardId = :boardId
    """,
    )
    fun deleteByBoardIdAndStatus(
        @Param("boardId") boardId: Long,
        @Param("status") status: Status = Status.DELETED,
    ): Int

    // post-processing
    @Modifying
    @Query(
        """
        update Comment c
           set c.postProcessing = true
         where c.commentId = :commentId
           and c.postProcessing = false
    """,
    )
    fun tryAcquirePostProcess(commentId: Long): Int

    @Modifying
    @Query(
        """
        update Comment c
           set c.postProcessing = false,
               c.postProcessedAt = CURRENT_TIMESTAMP,
               c.postProcessLastError = null
         where c.commentId = :commentId
    """,
    )
    fun releasePostProcessSuccess(commentId: Long): Int

    @Modifying
    @Query(
        """
        update Comment c
           set c.postProcessing = false,
               c.postProcessRetryCount = c.postProcessRetryCount + 1,
               c.postProcessLastError = :error
         where c.commentId = :commentId
    """,
    )
    fun releasePostProcessFail(
        commentId: Long,
        error: String,
    ): Int

    // 1. 완료 플레그라 false이며, 후처리 플래그가 false인 comment
    // 2. 완료 플레그가 false이며, 후처리 시간이 null 5분 이상 경과됐을 경우
    @Query(
        """
        select c
        from Comment c
        where c.isCompleted = false
          and (
                c.postProcessing = false
             or (c.postProcessing = true and c.postProcessedAt < :stuckBefore)
          )
        """,
    )
    fun findIncompleteOrStuckComments(
        @Param("stuckBefore") stuckBefore: LocalDateTime,
    ): List<Comment>
}
