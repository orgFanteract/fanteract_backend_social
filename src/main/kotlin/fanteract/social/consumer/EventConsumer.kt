package fanteract.social.consumer

import fanteract.social.adapter.AlarmWriter
import fanteract.social.adapter.CommentWriter
import fanteract.social.adapter.SagaSocialReader
import fanteract.social.adapter.SagaSocialWriter
import fanteract.social.dto.client.*
import fanteract.social.enumerate.EventStatus
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import fanteract.social.util.BaseUtil
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.Base64
import kotlin.String

@Component
@Transactional
class EventConsumer(
    private val alarmWriter: AlarmWriter,
    private val sagaSocialReader: SagaSocialReader,
    private val sagaSocialWriter: SagaSocialWriter,
    private val commentWriter: CommentWriter,
    private val kafkaTemplate: KafkaTemplate<String, String>,
) {
    @KafkaListener(
        topics = ["SOCIAL_SERVICE.createAlarm"],
        groupId = "social-service"
    )
    fun consumeCreateAlarm(message: String){
        println("consumeCreateAlarm exec")
        val decodedJson = String(Base64.getDecoder().decode(message))
        val response = BaseUtil.fromJson<MessageWrapper<CreateAlarmRequest>>(decodedJson)
        println(response)

        alarmWriter.create(
            userId = response.content.userId,
            targetUserId = response.content.targetUserId,
            contentType = response.content.contentType,
            contentId = response.content.contentId,
            alarmStatus = response.content.alarmStatus,
        )
    }

    @KafkaListener(
        topics = ["SOCIAL_SERVICE.createAlarmList"],
        groupId = "social-service"
    )
    fun consumeCreateAlarmList(message: String){
        println("consumeCreateAlarm exec")
        val decodedJson = String(Base64.getDecoder().decode(message))
        val response = BaseUtil.fromJson<MessageWrapper<CreateAlarmListRequest>>(decodedJson)

        for (targetId in response.content.targetUserIdList){
            alarmWriter.create(
                userId = response.content.userId,
                targetUserId = targetId,
                contentType = response.content.contentType,
                contentId = response.content.contentId,
                alarmStatus = response.content.alarmStatus,
            )
        }
    }

    /** 성공 시 작동 **/
    @KafkaListener(
        topics = [
            "SOCIAL_SERVICE.validateBoardStatusEvent.SUCCESS",
            "SOCIAL_SERVICE.filterCommentContentEvent.SUCCESS",
            "SOCIAL_SERVICE.createCommentEvent.SUCCESS",
            "SOCIAL_SERVICE.createAlarmToBoardUserEvent.SUCCESS",
            "SOCIAL_SERVICE.createAlarmToOtherCommentUserEvent.SUCCESS",
                 ],
        groupId = "social-service"
    )
    fun consumeCreateCommentEventSuccess(message: String){
        val decodedJson = String(Base64.getDecoder().decode(message))
        val response = BaseUtil.fromJson<EventWrapperForLog>(decodedJson)

        println("success event : ${response.eventName}")

        // 사가 트랜잭션 기록
        sagaSocialWriter.create(
            sagaId = response.sagaId,
            eventId = response.eventId,
            eventName = response.eventName,
            payload = response.payload?.toString(),
            eventStatus = EventStatus.SUCCESS,
            isExec = true,
        )
    }

    /** 실패 시 작동 **/
    @KafkaListener(
        topics = [
            "SOCIAL_SERVICE.validateBoardStatusEvent.FAIL",
            "SOCIAL_SERVICE.filterCommentContentEvent.FAIL",
            "SOCIAL_SERVICE.createCommentEvent.FAIL",
            "SOCIAL_SERVICE.createAlarmToBoardUserEvent.FAIL",
            "SOCIAL_SERVICE.createAlarmToOtherCommentUserEvent.FAIL",
                 ],
        groupId = "social-service"
    )
    fun consumeCreateCommentEventFail(message: String){
        val decodedJson = String(Base64.getDecoder().decode(message))
        val response = BaseUtil.fromJson<EventWrapperForLog>(decodedJson)

        println("fail event : ${response.eventName}")

        // 사가 트랜잭션 기록
        sagaSocialWriter.create(
            sagaId = response.sagaId,
            eventId = response.eventId,
            eventName = response.eventName,
            payload = response.payload?.toString(),
            eventStatus = EventStatus.FAIL,
            isExec = false,
        )
    }

    /** 보상 컨트롤러 : 살패 시 작동 */
    @KafkaListener(
        topics = [
            "SOCIAL_SERVICE.validateBoardStatusEvent.FAIL",
            "ACCOUNT_SERVICE.debitIfEnoughEvent.FAIL",
            "SOCIAL_SERVICE.filterCommentContentEvent.FAIL",
            "SOCIAL_SERVICE.createCommentEvent.FAIL",
            "ACCOUNT_SERVICE.updateActivePointEvent.FAIL",
            "SOCIAL_SERVICE.createAlarmToBoardUserEvent.FAIL",
            "SOCIAL_SERVICE.createAlarmToOtherCommentUserEvent.FAIL",
        ],
        groupId = "compensate-service"
    )
    fun createCommentEventCompensateManager(message: String){
        val decodedJson = String(Base64.getDecoder().decode(message))
        val response = BaseUtil.fromJson<EventWrapper<CreateCommentEventCompensateDto>>(decodedJson)

        println("compensate event : ${response.eventName}")

        // 에러 종류에 맞춰 보상 프로세스 메세지 전송
        when (response.eventName) {
            "validateBoardStatusEvent" -> {
                return
            }
            "debitIfEnoughEvent" -> {
                return
            }
            "filterCommentContentEvent" -> {
                kafkaTemplate.send(
                    "SOCIAL_SERVICE.debitIfEnoughEvent.COMPENSATE",
                    message,
                )
            }
            "createCommentEvent" -> {
                kafkaTemplate.send(
                    "SOCIAL_SERVICE.debitIfEnoughEvent.COMPENSATE",
                    message,
                )
            }
            "updateActivePointEvent" -> {
                kafkaTemplate.send(
                    "SOCIAL_SERVICE.createComment.createCommentEvent.COMPENSATE",
                    message,
                )
            }
            "createAlarmToBoardUserEvent" -> {
                kafkaTemplate.send(
                    "ACCOUNT_SERVICE.createComment.updateActivePointEvent.COMPENSATE",
                    message,
                )
            }
            "createAlarmToOtherCommentUserEvent" -> {
                kafkaTemplate.send(
                    "ACCOUNT_SERVICE.createComment.updateActivePointEvent.COMPENSATE",
                    message,
                )
            }
        }
    }

    /** 4번 보상 **/
    @KafkaListener(
        topics = [
            "SOCIAL_SERVICE.createComment.createCommentEvent.COMPENSATE",
        ],
        groupId = "social-service"
    )
    fun createCommentEventCompensate(message: String){
        println("compensate : createCommentEventCompensate")
        val decodedJson = String(Base64.getDecoder().decode(message))
        val response = BaseUtil.fromJson<EventWrapper<CreateCommentEventCompensateDto>>(decodedJson)

        // 보상 여부 확인
        if (sagaSocialReader.existsBySagaIdAndEventNameAndEventStatus(
                sagaId = response.sagaId,
                eventName = "createCommentEvent",
                eventStatus = EventStatus.COMPENSATE,
            )
        ){
            return
        }

        // 보상 로직 적용
        if (response.payload.commentId != null){
            commentWriter.deleteById(response.payload.commentId)
        } else {
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        // 보상 체이닝 메세지 전송
        kafkaTemplate.send(
            "ACCOUNT_SERVICE.createComment.debitIfEnoughEvent.COMPENSATE",
            message,
        )

        // 보상 로그 기록
        sagaSocialWriter.create(
            sagaId = response.sagaId,
            eventId = response.eventId,
            eventName = "createCommentEvent",
            payload = response.payload.toString(),
            eventStatus = EventStatus.COMPENSATE,
            isExec = true,
        )
    }

}