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
import fanteract.social.dto.client.CreateAlarmRequest
import fanteract.social.dto.client.UpdateActivePointRequest
import fanteract.social.dto.inner.*
import fanteract.social.dto.outer.*
import fanteract.social.entity.CommentHeart
import fanteract.social.enumerate.*
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.map

@Transactional
@Service
class HeartService(
    private val boardHeartReader: BoardHeartReader,
    private val boardHeartWriter: BoardHeartWriter,
    private val commentHeartReader: CommentHeartReader,
    private val commentHeartWriter: CommentHeartWriter,
    private val messageAdapter: MessageAdapter,
    private val accountClient: AccountClient,
    private val alarmReader: AlarmReader,
    private val alarmWriter: AlarmWriter,
    private val boardReader: BoardReader,
    private val commentReader: CommentReader,
    private val commentWriter: CommentWriter,
) {
    fun createHeartInBoard(boardId: Long, userId: Long): CreateHeartInBoardOuterResponse{
        // 코멘트 존재 여부 확인
        if (!boardReader.existsById(boardId)){
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        // 하트 입력(유니크 인덱스 기반)
        val boardHeart =
            try {
                boardHeartWriter.create(
                    userId = userId,
                    boardId = boardId,
                    status = Status.PENDING
                )
            } catch (e: DataIntegrityViolationException) { // 중복이 발생할 경우
                throw ExceptionType.withType(MessageType.ALREADY_EXIST)
            }

        // 사용자 비용 검증 및 차감
        try {
            val debitRes = accountClient.debitBalanceIfEnough2(userId, Balance.HEART.cost)

            if (debitRes.response == 0) {
                boardHeartWriter.delete(boardHeart)
                throw ExceptionType.withType(MessageType.NOT_ENOUGH_BALANCE)
            }

            boardHeartWriter.updateStatus(boardHeart, Status.ACTIVATED)
        }
        // 예상된 예외
        catch (e: ExceptionType) {
            throw e
        }
        // 예상치 못한 예외
        catch (e: Exception) {
            boardHeartWriter.delete(boardHeart)
            throw ExceptionType.withType(MessageType.INVALID_ACTION)
        }

        // 활동 점수 변경(비동기)
        messageAdapter.sendMessageUsingBroker(
            message =
                UpdateActivePointRequest(
                    userId = userId,
                    activePoint = ActivePoint.HEART.point
                ),
            topicService = TopicService.ACCOUNT_SERVICE,
            methodName = "updateActivePoint"
        )

        // 알람 전송(비동기)
        messageAdapter.sendMessageUsingBroker(
            message =
                CreateAlarmRequest(
                    userId = userId,
                    targetUserId = boardHeart.userId,
                    contentType = ContentType.BOARD_HEART,
                    contentId = boardHeart.boardHeartId,
                    alarmStatus = AlarmStatus.CREATED,
                ),
            topicService = TopicService.SOCIAL_SERVICE,
            methodName = "createAlarm"
        )


        return CreateHeartInBoardOuterResponse(boardHeart.boardHeartId)
    }

    fun deleteHeartInBoard(boardId: Long, userId: Long) {
        // 하트 해제
        boardHeartWriter.deleteByUserIdAndBoardId(
            userId = userId,
            boardId = boardId,
        )

        // 활동 점수 반납(비동기)
        messageAdapter.sendMessageUsingBroker(
            message =
                UpdateActivePointRequest(
                    userId = userId,
                    activePoint = -ActivePoint.HEART.point
                ),
            topicService = TopicService.ACCOUNT_SERVICE,
            methodName = "updateActivePoint"
        )
    }

    fun createHeartInComment(commentId: Long, userId: Long): CreateHeartInCommentOuterResponse {
        // 코멘트 존재 여부 확인
        if (!commentReader.existsById(commentId)){
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        // 하트 입력(유니크 인덱스 기반)
        val commentHeart =
            try {
                commentHeartWriter.create(
                    userId = userId,
                    commentId = commentId,
                    status = Status.PENDING
                )
            } catch (e: DataIntegrityViolationException) { // 중복이 발생할 경우
                throw ExceptionType.withType(MessageType.ALREADY_EXIST)
            }

        // 사용자 비용 검증 및 차감
        try {
            val debitRes = accountClient.debitBalanceIfEnough2(userId, Balance.HEART.cost)

            if (debitRes.response == 0) {
                commentHeartWriter.delete(commentHeart)
                throw ExceptionType.withType(MessageType.NOT_ENOUGH_BALANCE)
            }

            commentHeartWriter.updateStatus(commentHeart, Status.ACTIVATED)
        }
        // 예상된 예외
        catch (e: ExceptionType) {
            throw e
        }
        // 예상치 못한 예외
        catch (e: Exception) {
            commentHeartWriter.delete(commentHeart)
            throw ExceptionType.withType(MessageType.INVALID_ACTION)
        }

        // 활동 점수 변경(비동기)
        messageAdapter.sendMessageUsingBroker(
            message =
                UpdateActivePointRequest(
                    userId = userId,
                    activePoint = ActivePoint.HEART.point
                ),
            topicService = TopicService.ACCOUNT_SERVICE,
            methodName = "updateActivePoint"
        )

        // 알람 전송(비동기)
        messageAdapter.sendMessageUsingBroker(
            message =
                CreateAlarmRequest(
                    userId = userId,
                    targetUserId = commentHeart.userId,
                    contentType = ContentType.COMMENT_HEART,
                    contentId = commentHeart.commentHeartId,
                    alarmStatus = AlarmStatus.CREATED,
                ),
            topicService = TopicService.SOCIAL_SERVICE,
            methodName = "createAlarm"
        )

        return CreateHeartInCommentOuterResponse(commentHeart.commentHeartId)
    }

    fun deleteHeartInComment(commentId: Long, userId: Long) {
        // 하트 삭제
        commentHeartWriter.deleteByUserIdAndCommentId(
            userId = userId,
            commentId = commentId,
        )

        // 활동 점수 반납(비동기)
        messageAdapter.sendMessageUsingBroker(
            message =
                UpdateActivePointRequest(
                    userId = userId,
                    activePoint = -ActivePoint.HEART.point
                ),
            topicService = TopicService.ACCOUNT_SERVICE,
            methodName = "updateActivePoint"
        )
    }

    fun findBoardHeartByBoardIdIn(idList: List<Long>): BoardHeartListInnerResponse {
        val boardHeartList = boardHeartReader.findByBoardIdIn(idList)
        val payload =
            boardHeartList.map {
                BoardHeartInnerResponse(
                    boardHeartId = it.boardHeartId,
                    boardId = it.boardId,
                    userId = it.userId,
                )
        }

        return BoardHeartListInnerResponse(payload)
    }
    fun existsBoardHeartByUserIdAndBoardId(userId: Long, boardId: Long): Boolean {
        val isExist = boardHeartReader.existsByUserIdAndBoardId(userId,boardId)

        return isExist
    }

    fun existsCommentHeartByUserIdAndCommentId(userId: Long, commentId: Long): Boolean {
        val isExist = commentHeartReader.existsByUserIdAndCommentId(userId, commentId)

        return isExist
    }

    fun findByCommentIdIn(idList: List<Long>): List<CommentHeart>{
        val heartList = commentHeartReader.findByCommentIdIn(idList)

        return heartList
    }
}