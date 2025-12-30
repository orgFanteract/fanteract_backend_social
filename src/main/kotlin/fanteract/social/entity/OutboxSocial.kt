package fanteract.social.entity

import fanteract.social.entity.constant.BaseEntity
import fanteract.social.enumerate.OutboxStatus
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(
    name = "outbox_social",
    indexes = [
        Index(name = "idx_outbox_status_created", columnList = "outbox_status, created_at"),
        Index(name = "idx_outbox_event_id", columnList = "event_id", unique = true),
    ],
)
data class OutboxSocial(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val outboxId: Long = 0L,
    val eventId: String = UUID.randomUUID().toString(),
    val content: String, // 전송 내용. JSON을 base64로 인코딩 후 적재
    val topic: String, // 구독 대상
    @Enumerated(EnumType.STRING)
    var outboxStatus: OutboxStatus,
    val methodName: String,
) : BaseEntity()
