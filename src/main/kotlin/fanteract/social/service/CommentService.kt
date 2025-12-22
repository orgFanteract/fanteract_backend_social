package fanteract.social.service

import fanteract.social.client.AccountClient
import fanteract.social.adapter.AlarmReader
import fanteract.social.adapter.AlarmWriter
import fanteract.social.adapter.BoardHeartReader
import fanteract.social.adapter.BoardHeartWriter
import fanteract.social.adapter.BoardReader
import fanteract.social.adapter.CommentHeartReader
import fanteract.social.adapter.CommentHeartWriter
import fanteract.social.adapter.CommentReader
import fanteract.social.adapter.CommentWriter
import fanteract.social.adapter.MessageAdapter
import fanteract.social.dto.client.CreateAlarmListRequest
import fanteract.social.dto.client.CreateAlarmRequest
import fanteract.social.dto.client.UpdateActivePointRequest
import fanteract.social.dto.client.WriteCommentForUserRequest
import fanteract.social.dto.outer.*
import fanteract.social.dto.inner.*
import fanteract.social.enumerate.ActivePoint
import fanteract.social.enumerate.AlarmStatus
import fanteract.social.enumerate.Balance
import fanteract.social.enumerate.ContentType
import fanteract.social.enumerate.RiskLevel
import fanteract.social.enumerate.Status
import fanteract.social.enumerate.TopicService
import fanteract.social.enumerate.WriteStatus
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import fanteract.social.filter.ProfanityFilterService
import fanteract.social.orchestrator.CreateCommentOrchestratorV2
import fanteract.social.util.DeltaInMemoryStorage
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.Long
import kotlin.collections.associateBy
import kotlin.collections.count
import kotlin.collections.distinct
import kotlin.collections.groupBy
import kotlin.collections.map

@Transactional
@Service
class CommentService(
    private val alarmReader: AlarmReader,
    private val alarmWriter: AlarmWriter,
    private val boardReader: BoardReader,
    private val commentReader: CommentReader,
    private val commentWriter: CommentWriter,
    private val boardHeartReader: BoardHeartReader,
    private val boardHeartWriter: BoardHeartWriter,
    private val commentHeartReader: CommentHeartReader,
    private val commentHeartWriter: CommentHeartWriter,
    private val accountClient: AccountClient,
    private val profanityFilterService: ProfanityFilterService,
    private val messageAdapter: MessageAdapter,
    private val deltaInMemoryStorage: DeltaInMemoryStorage,
    private val commentEventService: CommentEventService,
    private val createCommentOrchestratorV2: CreateCommentOrchestratorV2
) {
    fun readCommentsByBoardId(
        boardId: Long,
        page: Int,
        size: Int
    ): ReadCommentPageOuterResponse {
        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        val commentPage = commentReader.findByBoardId(boardId, pageable)
        val commentContent = commentPage.content

        val heartGroup = commentHeartReader.findByCommentIdIn(commentContent.map {it.commentId}).groupBy { it.commentId }
        val userMap = accountClient.findByIdIn(commentContent.map {it.userId}).associateBy {it.userId }

        val payload =
            commentContent.map { comment ->
                val heart = heartGroup[comment.commentId]
                val user = userMap[comment.userId]!!

                ReadCommentResponse(
                    commentId = comment.commentId,
                    boardId = comment.boardId,
                    content = comment.content,
                    heartCount = heart?.count()?: 0,
                    userName = user.email,
                    createdAt = comment.createdAt!!,
                    updatedAt = comment.updatedAt!!,
                )
            }

        return ReadCommentPageOuterResponse(
            contents = payload,
            page = commentPage.number,
            size = commentPage.size,
            totalElements = commentPage.totalElements,
            totalPages = commentPage.totalPages,
            hasNext = commentPage.hasNext()
        )
    }

    fun readCommentsByUserId(
        userId: Long,
        page: Int,
        size: Int
    ): ReadCommentPageOuterResponse {
        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        val commentPage = commentReader.findByUserId(userId, pageable)
        val commentContent = commentPage.content

        val heartGroup = commentHeartReader.findByCommentIdIn(commentContent.map {it.commentId}).groupBy { it.commentId }
        val userMap = accountClient.findByIdIn(commentContent.map {it.userId}).associateBy {it.userId }

        val payload =
            commentContent.map { comment ->
                val heart = heartGroup[comment.commentId]
                val user = userMap[comment.userId]!!

                ReadCommentResponse(
                    commentId = comment.commentId,
                    boardId = comment.boardId,
                    content = comment.content,
                    heartCount = heart?.count()?: 0,
                    userName = user.email,
                    createdAt = comment.createdAt!!,
                    updatedAt = comment.updatedAt!!,
                )
            }

        return ReadCommentPageOuterResponse(
            contents = payload,
            page = commentPage.number,
            size = commentPage.size,
            totalElements = commentPage.totalElements,
            totalPages = commentPage.totalPages,
            hasNext = commentPage.hasNext()
        )
    }

    fun createComment(
        boardId: Long,
        userId: Long,
        createCommentOuterRequest: CreateCommentOuterRequest
    ): CreateCommentOuterResponse {
        // 게시글 상태 검증
        val board = boardReader.findById(boardId)

        if (board.riskLevel == RiskLevel.BLOCK || board.status == Status.DELETED){
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        // 사용자 잔액 차감
        val user = accountClient.findById(userId)

        if (user.balance < Balance.COMMENT.cost){
            throw ExceptionType.withType(MessageType.NOT_ENOUGH_BALANCE)
        }

        accountClient.updateBalance(userId, -Balance.COMMENT.cost)

        // 게시글 필터링 진행
        val riskLevel =
            profanityFilterService.checkProfanityAndUpdateAbusePoint(
                userId = userId,
                text = createCommentOuterRequest.content,
            )

        // 코멘트 생성
        val comment =
            commentWriter.create(
                boardId = boardId,
                userId = userId,
                content = createCommentOuterRequest.content,
                riskLevel = riskLevel,
            )

        // 활동 점수 변경(비동기)
        if (riskLevel != RiskLevel.BLOCK) {
            messageAdapter.sendMessageUsingBroker(
                message =
                    UpdateActivePointRequest(
                        userId = userId,
                        activePoint = ActivePoint.COMMENT.point
                    ),
                topicService = TopicService.ACCOUNT_SERVICE,
                methodName = "updateActivePoint"
            )
        }

        // 게시글 내 코멘트 작성자에게 알람 전송(비동기)
        val commentUserIdList = commentReader.findByBoardId(boardId).map {it.userId}.distinct()

        messageAdapter.sendMessageUsingBroker(
            message =
                CreateAlarmListRequest(
                    userId = userId,
                    targetUserIdList = commentUserIdList,
                    contentType = ContentType.COMMENT,
                    contentId = comment.boardId,
                    alarmStatus = AlarmStatus.CREATED,
                ),
            topicService = TopicService.SOCIAL_SERVICE,
            methodName = "createAlarmList"
        )

        // 게시글 작성자에게 알람 전송(비동기)
        val boardUserId = boardReader.findById(boardId).userId

        messageAdapter.sendMessageUsingBroker(
            message =
                CreateAlarmRequest(
                    userId = userId,
                    targetUserId = boardUserId,
                    contentType = ContentType.COMMENT,
                    contentId = comment.commentId,
                    alarmStatus = AlarmStatus.CREATED,
                ),
            topicService = TopicService.SOCIAL_SERVICE,
            methodName = "createAlarm"
        )

        val flag = false

        if (flag){
            // 카프카 기반 쓰기 행위 메세지 전달
            messageAdapter.sendMessageUsingBroker(
                message =
                    WriteCommentForUserRequest(
                        userId = userId,
                        writeStatus = WriteStatus.CREATED,
                        riskLevel = riskLevel,
                    ),
                topicService = TopicService.SOCIAL_SERVICE,
                methodName = "createCommentForUser"
            )
        }

        else {
            // 값 누적
            deltaInMemoryStorage.addDelta(userId, "commentCount", 1)

            if (riskLevel == RiskLevel.BLOCK) {
                deltaInMemoryStorage.addDelta(userId, "restrictedCommentCount", 1)
            }
        }

        // 반환
        return CreateCommentOuterResponse(
            commentId = comment.commentId,
            riskLevel = riskLevel,
        )
    }

    fun updateComment(commentId: Long, userId: Long, updateCommentOuterRequest: UpdateCommentOuterRequest) {
        val preComment = commentReader.findById(commentId)

        if (preComment.userId != userId) {
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        commentWriter.update(
            commentId = commentId,
            content = updateCommentOuterRequest.content,
        )
    }
    fun deleteComment(commentId: Long, userId: Long) {
        val preComment = commentReader.findById(commentId)

        if (preComment.userId != userId) {
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        commentWriter.delete(commentId = commentId)

        // 연결된 좋아요 삭제
        commentHeartWriter.deleteByCommentId(commentId)

        val flag = false

        if (flag){
            // 카프카 기반 쓰기 행위 메세지 전달
            messageAdapter.sendMessageUsingBroker(
                message =
                    WriteCommentForUserRequest(
                        userId = userId,
                        writeStatus = WriteStatus.DELETED,
                        riskLevel = preComment.riskLevel,
                    ),

                topicService = TopicService.SOCIAL_SERVICE,
                methodName = "deleteCommentForUser"
            )
        }

        else {
            // 값 누적
            deltaInMemoryStorage.addDelta(userId, "commentCount", -1)

            if (preComment.riskLevel == RiskLevel.BLOCK) {
                deltaInMemoryStorage.addDelta(userId, "restrictedCommentCount", -1)
            }
        }
    }

    fun countByUserId(userId: Long): ReadCommentCountInnerResponse {
        val response = commentReader.countByUserId(userId)
        return ReadCommentCountInnerResponse(response)
    }
    fun countByUserIdAndRiskLevel(userId: Long, riskLevel: RiskLevel): ReadCommentCountInnerResponse {
        val response = commentReader.countByUserIdAndRiskLevel(userId, riskLevel)

        return ReadCommentCountInnerResponse(response)
    }
    fun findByUserIdAndRiskLevel(
        page: Int,
        size: Int,
        userId: Long,
        riskLevel: RiskLevel
    ): ReadCommentPageInnerResponse {
        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        val commentPage = commentReader.findByUserIdAndRiskLevel(userId, riskLevel, pageable)
        val commentContent = commentPage.content.map { comment ->
            ReadCommentInnerResponse(
                commentId = comment.commentId,
                content = comment.content,
                boardId = comment.boardId,
                userId = comment.userId,
                riskLevel = comment.riskLevel,
                createdAt = comment.createdAt!!,
                updatedAt = comment.updatedAt!!,
            )
        }

        return ReadCommentPageInnerResponse(
            contents = commentContent,
            page = commentPage.number,
            size = commentPage.size,
            totalElements = commentPage.totalElements,
            totalPages = commentPage.totalPages,
            hasNext = commentPage.hasNext()
        )
    }

    fun existsById(commentId: Long): ReadCommentExistsInnerResponse {
        val response = commentReader.existsById(commentId)

        return ReadCommentExistsInnerResponse(response)
    }

    fun findById(commentId: Long): ReadCommentDetailInnerResponse {
        val comment = commentReader.findById(commentId)
        val user = accountClient.findById(comment.userId)
        val commentHeartList = commentHeartReader.findByCommentIdIn(listOf(commentId))

        return ReadCommentDetailInnerResponse(
            commentId = comment.commentId,
            content = comment.content,
            heartCount = commentHeartList.size,
            userName = user.name,
            userId = user.userId,
            createdAt = comment.createdAt!!,
            updatedAt = comment.updatedAt!!,
        )
    }

    fun findByBoardIdIn(boardIds: List<Long>): ReadCommentListInnerResponse {
        val commentList = commentReader.findByBoardIdIn(boardIds)

        val payload = commentList.map { comment ->
            ReadCommentInnerResponse(
                commentId = comment.commentId,
                content = comment.content,
                boardId = comment.boardId,
                userId = comment.userId,
                riskLevel = comment.riskLevel,
                createdAt = comment.createdAt!!,
                updatedAt = comment.updatedAt!!,
            )
        }

        return ReadCommentListInnerResponse(
            contents = payload
        )
    }

    fun createCommentWithChoreography(
        boardId: Long,
        userId: Long,
        createCommentOuterRequest: CreateCommentOuterRequest
    ) {
        commentEventService.validateBoardStatusEvent(
            boardId = boardId,
            userId = userId,
            createCommentOuterRequest = createCommentOuterRequest
        )
    }

    fun createCommentWithOrchestrationV2(
        boardId: Long,
        userId: Long,
        createCommentOuterRequest: CreateCommentOuterRequest
    ) {
        createCommentOrchestratorV2.start(
            boardId = boardId,
            userId = userId,
            req = createCommentOuterRequest
        )
    }


}