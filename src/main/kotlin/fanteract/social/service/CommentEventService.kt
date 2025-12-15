package fanteract.social.service

import fanteract.social.adapter.BoardReader
import fanteract.social.adapter.CommentReader
import fanteract.social.adapter.CommentWriter
import fanteract.social.adapter.MessageAdapter
import fanteract.social.consumer.EventConsumer
import fanteract.social.dto.client.*
import fanteract.social.dto.outer.CreateCommentOuterRequest
import fanteract.social.enumerate.AlarmStatus
import fanteract.social.enumerate.ContentType
import fanteract.social.enumerate.EventStatus
import fanteract.social.enumerate.RiskLevel
import fanteract.social.enumerate.Status
import fanteract.social.enumerate.TopicService
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import fanteract.social.filter.ProfanityFilterService
import fanteract.social.util.messageResolver
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component
import java.util.UUID
import kotlin.String

@Component
class CommentEventService(
    private val boardReader: BoardReader,
    private val messageAdapter: MessageAdapter,
    private val profanityFilterService: ProfanityFilterService,
    private val commentWriter: CommentWriter,
    private val commentReader: CommentReader,
) {
    /** 1번 **/
    fun validateBoardStatusEvent(
        boardId: Long,
        userId: Long,
        createCommentOuterRequest: CreateCommentOuterRequest,
    ){
        // init
        println("event! = validateBoardStatusEvent")
        val sagaId = "SAGA-${UUID.randomUUID()}"

        // exec
        try{
            val board = boardReader.findById(boardId)

            if (board.riskLevel == RiskLevel.BLOCK || board.status == Status.DELETED){
                throw ExceptionType.withType(MessageType.NOT_EXIST)
            }

            // send message
            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "debitIfEnoughEvent",
                causationId = null,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.PROCESS,
                payload = DebitIfEnoughEventDto(
                    userId = userId,
                    boardId = boardId,
                    content = createCommentOuterRequest.content,
                ),
            )

            // send success message
            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "validateBoardStatusEvent",
                causationId = null,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.SUCCESS,
                payload = ValidateBoardStatusDto(
                    boardId = boardId,
                    userId = userId,
                    content = createCommentOuterRequest.content
                ),
            )
        } catch (e: Exception){
            // send fail message
            messageAdapter.sendEventUsingBroker(
                sagaId = sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "validateBoardStatusEvent",
                causationId = null,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.FAIL,
                payload = CreateCommentEventCompensateDto(
                    userId = null,
                    refundCost = null,
                    refundActivePoint = null,
                    commentId = null,
                )
            )
        }

    }
    
    /** 3번 **/
    @KafkaListener(
        topics = ["SOCIAL_SERVICE.filterCommentContentEvent.PROCESS"],
        groupId = "social-service"
    )
    fun filterCommentContentEvent(message: String) {
        println("event! = filterCommentContentEvent")

        // receive message
        val response = messageResolver<FilterCommentContentEventDto>(message)

        try {
            // exec
            val riskLevel =
                profanityFilterService.checkProfanityAndUpdateAbusePoint(
                    userId = response.payload.userId,
                    text = response.payload.content,
                )

            // send message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "createCommentEvent",
                causationId = response.eventId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.PROCESS,
                payload = CreateCommentEventDto(
                    boardId = response.payload.boardId,
                    userId = response.payload.userId,
                    content = response.payload.content,
                    riskLevel = riskLevel,
                    cost = response.payload.cost
                ),
            )

            // send success message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "filterCommentContentEvent",
                causationId = response.eventId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.SUCCESS,
                payload = response.payload
            )
        } catch (e: Exception){
            // send fail message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "filterCommentContentEvent",
                causationId = response.eventId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.FAIL,
                payload = CreateCommentEventCompensateDto(
                    userId = response.payload.userId,
                    refundCost = response.payload.cost,
                    refundActivePoint = null,
                    commentId = null,
                )
            )
        }
    }
    /** 4번 **/
    @KafkaListener(
        topics = ["SOCIAL_SERVICE.createCommentEvent.PROCESS"],
        groupId = "social-service"
    )
    fun createCommentEvent(message: String){
        println("event! = createCommentEvent")

        // receive message
        val response = messageResolver<CreateCommentEventDto>(message)
        try {
            // exec
            val comment =
                commentWriter.create(
                    boardId = response.payload.boardId,
                    userId = response.payload.userId,
                    content = response.payload.content,
                    riskLevel = response.payload.riskLevel,
                )

            // send message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "updateActivePointEvent",
                causationId = response.eventId,
                topicService = TopicService.ACCOUNT_SERVICE,
                eventStatus = EventStatus.PROCESS,
                payload = UpdateActivePointEventDto(
                    boardId = response.payload.boardId,
                    userId = response.payload.userId,
                    commentId = comment.commentId,
                    cost = response.payload.cost
                ),
            )

            // send success message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "createCommentEvent",
                causationId = response.eventId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.SUCCESS,
                payload = response.payload
            )
        } catch (e: Exception){
            // send fail message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "createCommentEvent",
                causationId = response.eventId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.FAIL,
                payload = CreateCommentEventCompensateDto(
                    userId = response.payload.userId,
                    refundCost = response.payload.cost,
                    refundActivePoint = null,
                    commentId = null,
                )
            )
        }
    }
    
    @KafkaListener(
        topics = ["SOCIAL_SERVICE.createAlarmToBoardUserEvent.PROCESS"],
        groupId = "social-service"
    )
    /** 6번 **/
    fun createAlarmToBoardUserEvent(message: String){
        println("event! = createAlarmToBoardUserEvent")

        // receive message
        val response = messageResolver<CreateAlarmToBoardUserEventDto>(message)

        try {
            // exec
            val boardUserId = boardReader.findById(response.payload.boardId).userId

            messageAdapter.sendMessageUsingBroker(
                message =
                    CreateAlarmRequest(
                        userId = response.payload.userId,
                        targetUserId = boardUserId,
                        contentType = ContentType.BOARD,
                        contentId = response.payload.boardId,
                        alarmStatus = AlarmStatus.CREATED,
                    ),
                topicService = TopicService.SOCIAL_SERVICE,
                methodName = "createAlarm"
            )

            // send message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "createAlarmToOtherCommentUserEvent",
                causationId = response.eventId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.PROCESS,
                payload = CreateAlarmToOtherCommentUserEventDto(
                    userId = response.payload.userId,
                    contentId = response.payload.boardId,
                    cost = response.payload.cost,
                    activePoint = response.payload.activePoint,
                    commentId = response.payload.commentId,
                ),
            )

            // send success message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "createAlarmToBoardUserEvent",
                causationId = response.eventId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.SUCCESS,
                payload = response.payload,
            )
        } catch (e: Exception){
            // send fail message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "createAlarmToBoardUserEvent",
                causationId = response.eventId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.FAIL,
                payload = CreateCommentEventCompensateDto(
                    userId = response.payload.userId,
                    refundCost = response.payload.cost,
                    refundActivePoint = response.payload.activePoint,
                    commentId = response.payload.commentId,
                )
            )
        }

    }


    /** 7번 **/
    @KafkaListener(
        topics = ["SOCIAL_SERVICE.createAlarmToOtherCommentUserEvent.PROCESS"],
        groupId = "social-service"
    )
    fun createAlarmToOtherCommentUserEvent(message: String){
        println("event! = createAlarmToOtherCommentUserEvent")

        // receive message
        val response = messageResolver<CreateAlarmToOtherCommentUserEventDto>(message)

        try {
            // exec
            val commentUserIdList = commentReader.findByBoardId(response.payload.contentId).map {it.userId}.distinct()

            messageAdapter.sendMessageUsingBroker(
                message =
                    CreateAlarmListRequest(
                        userId = response.payload.userId,
                        targetUserIdList = commentUserIdList,
                        contentType = ContentType.BOARD,
                        contentId = response.payload.contentId,
                        alarmStatus = AlarmStatus.CREATED,
                    ),
                topicService = TopicService.SOCIAL_SERVICE,
                methodName = "createAlarmList"
            )

            // send success message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "createAlarmToOtherCommentUserEvent",
                causationId = response.eventId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.SUCCESS,
                payload = response.payload,
            )
        } catch (e: Exception){
            // send fail message
            messageAdapter.sendEventUsingBroker(
                sagaId = response.sagaId,
                eventId = "EVENT-${UUID.randomUUID()}",
                eventName = "createAlarmToOtherCommentUserEvent",
                causationId = response.eventId,
                topicService = TopicService.SOCIAL_SERVICE,
                eventStatus = EventStatus.FAIL,
                payload = CreateCommentEventCompensateDto(
                    userId = response.payload.userId,
                    refundCost = response.payload.cost,
                    refundActivePoint = response.payload.activePoint,
                    commentId = response.payload.commentId,
                )
            )
        }

        // end
        println("end !")
    }
}