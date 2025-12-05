package fanteract.social.service

import fanteract.social.domain.AlarmReader
import fanteract.social.domain.AlarmWriter
import fanteract.social.dto.inner.CreateAlarmInnerRequest
import fanteract.social.dto.inner.CreateAlarmInnerResponse
import fanteract.social.dto.outer.ReadAlarmListOuterResponse
import fanteract.social.dto.outer.ReadAlarmOuterResponse
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import kotlin.collections.map

@Service
class AlarmService(
    private val alarmReader: AlarmReader,
    private val alarmWriter: AlarmWriter,
) {
    fun readAlarmByUserId(
        targetUserId: Long,
        page: Int,
        size: Int,
    ): ReadAlarmListOuterResponse {
        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt") // 최신 알람부터
        )

        val alarmPage = alarmReader.findByTargetUserId(targetUserId, pageable)
        val alarmContent = alarmPage.content

        val contents = alarmContent.map { alarm ->
            ReadAlarmOuterResponse(
                alarmId = alarm.alarmId,
                userId = alarm.userId,
                targetUserId = alarm.targetUserId,
                contentId = alarm.contentId,
                contentType = alarm.contentType,
                alarmStatus = alarm.alarmStatus,
            )
        }

        return ReadAlarmListOuterResponse(
            contents = contents,
            page = page,
            size = size,
            totalElements = alarmPage.totalElements,
            totalPages = alarmPage.totalPages,
            hasNext = alarmPage.hasNext()
        )
    }

    fun create(
        createAlarmInnerRequest: CreateAlarmInnerRequest,
        userId: Long
    ): CreateAlarmInnerResponse {
        val response =
            alarmWriter.create(
                userId = userId,
                targetUserId = createAlarmInnerRequest.targetUserId,
                contentType = createAlarmInnerRequest.contentType,
                contentId = createAlarmInnerRequest.contentId,
                alarmStatus = createAlarmInnerRequest.alarmStatus,
            )

        return CreateAlarmInnerResponse(response.alarmId)
    }

}