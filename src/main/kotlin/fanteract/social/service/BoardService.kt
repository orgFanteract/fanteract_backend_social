package fanteract.social.service

import fanteract.social.client.UserClient
import fanteract.social.domain.BoardHeartReader
import fanteract.social.domain.BoardHeartWriter
import fanteract.social.filter.ProfanityFilterService
import fanteract.social.domain.BoardReader
import fanteract.social.domain.BoardWriter
import fanteract.social.domain.CommentHeartReader
import fanteract.social.domain.CommentHeartWriter
import fanteract.social.domain.CommentReader
import fanteract.social.domain.CommentWriter
import fanteract.social.dto.inner.*
import fanteract.social.dto.outer.*
import fanteract.social.enumerate.ActivePoint
import fanteract.social.enumerate.Balance
import fanteract.social.enumerate.RiskLevel
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
    private val commentHeartReader: CommentHeartReader,
    private val commentHeartWriter: CommentHeartWriter,
    private val userClient: UserClient,
    private val profanityFilterService: ProfanityFilterService,
) {
    fun createBoard(
        createBoardOuterRequest: CreateBoardOuterRequest,
        userId: Long
    ): CreateBoardOuterResponse {
        // 사용자 잔여 포인트 확인
        val user = userClient.findById(userId)
        
        if (user.balance < Balance.BOARD.cost){
            throw kotlin.IllegalArgumentException("비용이 부족합니다")
        }

        userClient.updateBalance(userId, -Balance.BOARD.cost)
        
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

        // 활동 점수 변경
        if (riskLevel != RiskLevel.BLOCK) {
            userClient.updateActivePoint(
                userId = userId,
                activePoint = ActivePoint.BOARD.point
            )
        }

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
            userClient.findByIdIn(boardContent.map {it.userId}).associateBy { it.userId }


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
            userClient.findByIdIn(boardContent.map {it.userId}).associateBy { it.userId }

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
            throw NoSuchElementException("조건에 맞는 게시글이 존재하지 않습니다")
        }

        val commentList = commentReader.findByBoardIdIn(listOf(board.boardId))
        val heartList = boardHeartReader.findByBoardIdIn(listOf(board.boardId))
        val user = userClient.findById(board.userId)

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
            throw kotlin.NoSuchElementException("조건에 맞는 게시글이 존재하지 않습니다")
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
        val user = userClient.findById(board.userId)
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
}