package fanteract.social.repo

import fanteract.social.entity.OutboxSocial
import fanteract.social.enumerate.OutboxStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface OutboxSocialRepo : JpaRepository<OutboxSocial, Long> {
    fun findTop500ByOutboxStatusAndMethodNameOrderByCreatedAtAsc(
        status: OutboxStatus,
        methodName: String,
    ): List<OutboxSocial>

    fun findAllByOutboxStatusAndMethodNameOrderByCreatedAtDesc(
        outboxStatus: OutboxStatus,
        methodName: String,
    ): List<OutboxSocial>

    @Modifying
    @Query(
        "update OutboxSocial o " +
            "set o.outboxStatus = :status " +
            "where o.outboxId in :ids",
    )
    fun bulkUpdateStatus(
        @Param("status") status: OutboxStatus,
        @Param("ids") ids: List<Long>,
    ): Int
}
