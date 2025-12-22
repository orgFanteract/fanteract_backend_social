package fanteract.social.entity

import fanteract.social.entity.constant.BaseEntity
import fanteract.social.enumerate.SagaStatus
import jakarta.persistence.*


@Entity
@Table(name = "saga_instance")
class SagaInstance(
    @Id
    val sagaId: String,
    val sagaType: String,
    @Enumerated(EnumType.STRING)
    var sagaStatus: SagaStatus,
    var step: String,
    var contextJson: String, // 입력값, 중간 결과, 보상값
    var resultJson: String? = null, // 최종 결과
    @Version
    var version: Long? = null,
): BaseEntity()
