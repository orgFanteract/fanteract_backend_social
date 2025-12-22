package fanteract.social.service

import fanteract.social.client.AccountClient
import fanteract.social.adapter.BoardHeartReader
import fanteract.social.adapter.BoardHeartWriter
import fanteract.social.filter.ProfanityFilterService
import fanteract.social.adapter.BoardReader
import fanteract.social.adapter.BoardWriter
import fanteract.social.adapter.CommentHeartReader
import fanteract.social.adapter.CommentHeartWriter
import fanteract.social.adapter.CommentReader
import fanteract.social.adapter.CommentWriter
import fanteract.social.adapter.MessageAdapter
import fanteract.social.dto.client.UpdateActivePointRequest
import fanteract.social.dto.client.WriteCommentForUserRequest
import fanteract.social.dto.inner.*
import fanteract.social.dto.outer.*
import fanteract.social.enumerate.ActivePoint
import fanteract.social.enumerate.Balance
import fanteract.social.enumerate.RiskLevel
import fanteract.social.enumerate.TopicService
import fanteract.social.enumerate.WriteStatus
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import fanteract.social.util.DeltaInMemoryStorage
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.Long
import kotlin.collections.associateBy
import kotlin.collections.count
import kotlin.collections.groupBy
import kotlin.collections.map

@Transactional
@Service
class BoardService(
    private val boardReader: BoardReader,
    private val boardWriter: BoardWriter,
    private val commentReader: CommentReader,
    private val commentWriter: CommentWriter,
    private val boardHeartReader: BoardHeartReader,
    private val boardHeartWriter: BoardHeartWriter,
    private val commentHeartWriter: CommentHeartWriter,
    private val accountClient: AccountClient,
    private val profanityFilterService: ProfanityFilterService,
    private val messageAdapter: MessageAdapter,
    private val deltaInMemoryStorage: DeltaInMemoryStorage
) {
    fun createBoard(
        createBoardOuterRequest: CreateBoardOuterRequest,
        userId: Long
    ): CreateBoardOuterResponse {
        // 사용자 잔여 포인트 확인 및 차감
        val user = accountClient.findById(userId)

        if (user.balance < Balance.BOARD.cost){
            throw ExceptionType.withType(MessageType.NOT_ENOUGH_BALANCE)
        }

         accountClient.updateBalance(userId, -Balance.BOARD.cost)
        
        // 게시글 필터링 진행
        val riskLevel =
            profanityFilterService.checkProfanityAndUpdateAbusePoint(
                userId = userId,
                text = "${createBoardOuterRequest.title}\n${createBoardOuterRequest.content}",
            )

        // 게시글 생성
        val board =
            boardWriter.create(
                title = createBoardOuterRequest.title,
                content = createBoardOuterRequest.content,
                userId = userId,
                riskLevel = riskLevel,
            )

        // 활동 점수 변경(비동기)
        if (riskLevel != RiskLevel.BLOCK) {
            messageAdapter.sendMessageUsingBroker(
                message =
                    UpdateActivePointRequest(
                        userId = userId,
                        activePoint = ActivePoint.BOARD.point
                    ),
                topicService = TopicService.ACCOUNT_SERVICE,
                methodName = "updateActivePoint"
            )
        }

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
                methodName = "createBoardForUser"
            )
        }

        else {
            // 값 누적
            deltaInMemoryStorage.addDelta(userId, "boardCount", 1)

            if (riskLevel == RiskLevel.BLOCK) {
                deltaInMemoryStorage.addDelta(userId, "restrictedBoardCount", -1)
            }
        }



        // 결과 반환
        return CreateBoardOuterResponse(
            boardId = board.boardId,
            riskLevel = riskLevel,
        )
    }

    fun readBoardByUserId(page: Int, size: Int, userId: Long): ReadBoardListOuterResponse {
        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        val boardPage = boardReader.readByUserId(pageable, userId)
        val boardContent = boardPage.content

        val commentGroup =
            commentReader
                .findByBoardIdIn(boardContent.map {it.boardId})
                .groupBy { it.boardId }

        val heartGroup =
            boardHeartReader
                .findByBoardIdIn(boardContent.map {it.boardId})
                .groupBy {it.boardId }

        val userMap =
            accountClient.findByIdIn(boardContent.map {it.userId}).associateBy { it.userId }


        val payload =
            boardContent.map { board ->
                val comment = commentGroup[board.boardId]
                val heart = heartGroup[board.boardId]
                val user = userMap[board.userId]

                ReadBoardDetailOuterResponse(
                    boardId = board.boardId,
                    title = board.title,
                    content = board.content,
                    userName = user?.name ?: "-",
                    commentCount = comment?.count() ?: 0,
                    heartCount = heart?.count() ?: 0,
                    createdAt = board.createdAt!!,
                    updatedAt = board.updatedAt!!,
                )
            }

        return ReadBoardListOuterResponse(
            contents = payload,
            page = page,
            size = size,
            totalElements = boardPage.totalElements,
            totalPages = boardPage.totalPages,
            hasNext = boardPage.hasNext()
        )
    }

    fun readBoard(page: Int, size: Int): ReadBoardListOuterResponse {
        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        val boardPage =
            boardReader.findAllExceptBlock(
                pageable = pageable
            )

        val boardContent = boardPage.content

        val commentGroup =
            commentReader
                .findByBoardIdIn(boardContent.map {it.boardId})
                .groupBy { it.boardId }

        val heartGroup =
            boardHeartReader
                .findByBoardIdIn(boardContent.map {it.boardId})
                .groupBy {it.boardId }

        val userMap =
            accountClient.findByIdIn(boardContent.map {it.userId}).associateBy { it.userId }

        val payload =
            boardContent.map { board ->
                val comment = commentGroup[board.boardId]
                val heart = heartGroup[board.boardId]
                val user = userMap[board.userId]

                ReadBoardDetailOuterResponse(
                    boardId = board.boardId,
                    title = board.title,
                    content = board.content,
                    userName = user?.name ?: "-",
                    commentCount = comment?.count() ?: 0,
                    heartCount = heart?.count() ?: 0,
                    createdAt = board.createdAt!!,
                    updatedAt = board.updatedAt!!,
                )
            }


        return ReadBoardListOuterResponse(
            contents = payload,
            page = page,
            size = size,
            totalElements = boardPage.totalElements,
            totalPages = boardPage.totalPages,
            hasNext = boardPage.hasNext()
        )
    }

    fun readBoardDetail(
        boardId: Long
    ): ReadBoardDetailOuterResponse {
        val board = boardReader.findById(boardId)

        if (board.riskLevel == RiskLevel.BLOCK){
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        val commentList = commentReader.findByBoardIdIn(listOf(board.boardId))
        val heartList = boardHeartReader.findByBoardIdIn(listOf(board.boardId))
        val user = accountClient.findById(board.userId)

        return ReadBoardDetailOuterResponse(
            boardId = board.boardId,
            title = board.title,
            content = board.content,
            userName = user.name,
            commentCount = commentList.count(),
            heartCount = heartList.count(),
            createdAt = board.createdAt!!,
            updatedAt = board.updatedAt!!,
        )
    }

    fun updateBoard(
        boardId: Long,
        userId: Long,
        updateBoardOuterRequest: UpdateBoardOuterRequest
    ) {
        val preBoard = boardReader.findById(boardId)

        if (preBoard.userId != userId){
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        boardWriter
            .update(
                boardId = boardId,
                title = updateBoardOuterRequest.title,
                content = updateBoardOuterRequest.content,
            )
    }

    fun countByUserId(userId: Long): ReadBoardCountInnerResponse {
        val response = boardReader.countByUserId(userId)
        return ReadBoardCountInnerResponse(response)
    }

    fun countByUserIdAndRiskLevel(userId: Long, riskLevel: RiskLevel): ReadBoardCountInnerResponse {
        val response = boardReader.countByUserIdAndRiskLevel(userId, riskLevel)

        return ReadBoardCountInnerResponse(response)
    }
    fun findByUserIdAndRiskLevel(page: Int, size: Int, userId: Long, riskLevel: RiskLevel): ReadBoardPageInnerResponse {
        val pageable = PageRequest.of(
            page,
            size,
            Sort.by(Sort.Direction.DESC, "createdAt")
        )

        val boardPage = boardReader.findByUserIdAndRiskLevel(userId, riskLevel, pageable)
        val boardList = boardPage.content.map { board ->
            ReadBoardInnerResponse(
                boardId = board.boardId,
                title = board.title,
                content = board.content,
                userId = board.userId,
                riskLevel = board.riskLevel,
            )
        }

        val response =
            ReadBoardPageInnerResponse(
                contents = boardList,
                page = page,
                size = size,
                totalElements = boardPage.totalElements,
                totalPages = boardPage.totalPages,
                hasNext = boardPage.hasNext()
            )

        return response
    }

    fun existsById(boardId: Long): ReadBoardExistsInnerResponse {
        val response = boardReader.existsById(boardId)

        return ReadBoardExistsInnerResponse(response)
    }
    fun findById(boardId: Long): ReadBoardDetailInnerResponse {
        val board = boardReader.findById(boardId)
        val user = accountClient.findById(board.userId)
        val commentList = commentReader.findByBoardIdIn(listOf(board.boardId))
        val heartList = boardHeartReader.findByBoardIdIn(listOf(board.boardId))

        return ReadBoardDetailInnerResponse(
            boardId = board.boardId,
            userName = user.name,
            userId = user.userId,
            title = board.title,
            commentCount = commentList.size,
            heartCount = heartList.size,
            createdAt = board.createdAt!!,
            updatedAt = board.updatedAt!!
        )
    }

    fun deleteBoard(boardId: Long, userId: Long) {
        // board 검증
        val board = boardReader.findById(boardId)

        if (board.userId != userId){
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        // board 비활성화
        boardWriter.delete(board)

        // board 좋아요 삭제
        boardHeartWriter.deleteByBoardId(boardId)

        // comment 비활성화
        commentWriter.deleteByBoardId(boardId)

        // comment 좋아요 삭제
        val commentList = commentReader.findByBoardId(boardId)

        commentHeartWriter.deleteByCommentIdIn(commentList.map{it.commentId})
    }
}