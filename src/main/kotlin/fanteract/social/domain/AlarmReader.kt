package fanteract.social.domain

import fanteract.social.entity.Alarm
import fanteract.social.repo.AlarmRepo
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class AlarmReader(
    private val alarmRepo: AlarmRepo,
) {

    fun findByTargetUserId(
        targetUserId: Long,
        pageable: Pageable
    ): Page<Alarm> {
        return alarmRepo.findByTargetUserId(targetUserId, pageable)
    }
}