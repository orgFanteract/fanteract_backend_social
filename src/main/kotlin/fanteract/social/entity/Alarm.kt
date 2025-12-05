package fanteract.social.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import fanteract.social.entity.constant.BaseEntity
import fanteract.social.enumerate.AlarmStatus
import fanteract.social.enumerate.ContentType

@Entity
@Table(name = "alarms")
class Alarm (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val alarmId: Long = 0L,
    val userId: Long, // 알림을 던지는 주체
    val targetUserId: Long, // 알람을 받는 주체
    val contentId: Long,
    @Enumerated(EnumType.STRING)
    val contentType: ContentType,
    @Enumerated(EnumType.STRING)
    val alarmStatus: AlarmStatus,
): BaseEntity()