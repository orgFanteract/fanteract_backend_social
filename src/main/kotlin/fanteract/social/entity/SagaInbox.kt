package fanteract.social.entity

import fanteract.social.entity.constant.BaseEntity
import jakarta.persistence.*

@Entity
@Table(
    name = "saga_inboxes",
    uniqueConstraints = [UniqueConstraint(columnNames = ["sagaId", "eventId"])],
)
class SagaInbox(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val sagaId: String,
    val eventId: String,
    val eventName: String,
) : BaseEntity()
