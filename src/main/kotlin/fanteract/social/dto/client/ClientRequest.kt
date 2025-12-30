package fanteract.social.dto.client

import com.fasterxml.jackson.databind.JsonNode
import fanteract.social.enumerate.AlarmStatus
import fanteract.social.enumerate.ContentType
import fanteract.social.enumerate.EventStatus
import fanteract.social.enumerate.RiskLevel
import fanteract.social.enumerate.WriteStatus
import java.time.Instant

data class CreateAlarmRequest(
    val userId: Long, // 알림을 던지는 주체
    val targetUserId: Long, // 알람을 받는 주체
    val contentId: Long,
    val contentType: ContentType,
    val alarmStatus: AlarmStatus,
)

data class CreateAlarmListRequest(
    val userId: Long, // 알림을 던지는 주체
    val targetUserIdList: List<Long>, // 알람을 받는 주체
    val contentId: Long, // 단일 적용 대상
    val contentType: ContentType,
    val alarmStatus: AlarmStatus,
)

data class WriteCommentForUserRequest(
    val userId: Long,
    val writeStatus: WriteStatus,
    val riskLevel: RiskLevel,
)
