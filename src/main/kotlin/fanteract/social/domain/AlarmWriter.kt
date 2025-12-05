package fanteract.social.domain

import fanteract.social.enumerate.AlarmStatus
import fanteract.social.enumerate.ContentType
import fanteract.social.entity.Alarm
import fanteract.social.repo.AlarmRepo
import org.springframework.stereotype.Component
import kotlin.Long

@Component
class AlarmWriter(
    private val alarmRepo: AlarmRepo,
) {
    fun create(
        userId: Long,
        targetUserId: Long,
        contentType: ContentType,
        contentId: Long,
        alarmStatus: AlarmStatus,
    ): Alarm {
        return alarmRepo.save(
                Alarm(
                    userId = userId,
                    targetUserId = targetUserId,
                    contentId = contentId,
                    contentType = contentType,
                    alarmStatus = alarmStatus,
                )
            )
    }

}