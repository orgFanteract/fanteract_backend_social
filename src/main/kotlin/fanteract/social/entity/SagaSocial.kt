package fanteract.social.entity

import fanteract.social.entity.constant.BaseEntity
import fanteract.social.enumerate.EventStatus
import jakarta.persistence.*

@Entity
@Table(name = "saga_social")
class SagaSocial (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L,
    val sagaId: String,
    val eventId: String,
    val eventName: String,
    val payload: String?,
    @Enumerated(EnumType.STRING)
    val eventStatus: EventStatus,
    val isExec: Boolean = false,

): BaseEntity()