package fanteract.social.dto.inner

import fanteract.social.enumerate.AlarmStatus
import fanteract.social.enumerate.ContentType

data class CreateAlarmInnerRequest(
    val userId: Long, // 알림을 던지는 주체
    val targetUserId: Long, // 알람을 받는 주체
    val contentId: Long,
    val contentType: ContentType,
    val alarmStatus: AlarmStatus,
)