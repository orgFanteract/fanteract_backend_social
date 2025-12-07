package fanteract.social.service

import fanteract.social.client.UserClient
import fanteract.social.domain.AlarmReader
import fanteract.social.domain.AlarmWriter
import fanteract.social.domain.BoardHeartReader
import fanteract.social.domain.BoardHeartWriter
import fanteract.social.domain.BoardReader
import fanteract.social.domain.CommentHeartReader
import fanteract.social.domain.CommentHeartWriter
import fanteract.social.domain.CommentReader
import fanteract.social.domain.CommentWriter
import fanteract.social.dto.inner.*
import fanteract.social.dto.outer.*
import fanteract.social.entity.CommentHeart
import fanteract.social.enumerate.*
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
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
    private val userClient: UserClient,
    private val alarmReader: AlarmReader,
    private val alarmWriter: AlarmWriter,
    private val boardReader: BoardReader,
    private val commentReader: CommentReader,
    private val commentWriter: CommentWriter,
) {
    fun createHeartInBoard(boardId: Long, userId: Long): CreateHeartInBoardOuterResponse{
        // 비용 검증 및 차감
        val user = userClient.findById(userId)

        if (user.balance < Balance.HEART.cost){
            throw ExceptionType.withType(MessageType.NOT_ENOUGH_BALANCE)
        }

        userClient.updateBalance(userId, -Balance.HEART.cost)

        // 존재 여부 검증
        if (boardHeartReader.existsByUserIdAndBoardId(userId, boardId)){
            throw ExceptionType.withType(MessageType.ALREADY_EXIST)
        }

        if (!boardReader.existsById(boardId)){
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        val boardHeart =
            boardHeartWriter.create(
                userId = userId,
                boardId = boardId,
            )

        // 활동 점수 변경
        userClient.updateActivePoint(
            userId = userId,
            activePoint = ActivePoint.HEART.point
        )

        val board = boardReader.findById(boardHeart.boardId)

        // 알림 전송
        alarmWriter.create(
            userId = userId,
            targetUserId = board.userId,
            contentType = ContentType.BOARD_HEART,
            contentId = boardHeart.boardHeartId,
            alarmStatus = AlarmStatus.CREATED,
        )

        return CreateHeartInBoardOuterResponse(boardHeart.boardHeartId)
    }

    fun deleteHeartInBoard(boardId: Long, userId: Long) {
        if (!boardReader.existsById(boardId)){
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        // 하트 해제
        boardHeartWriter.delete(
            userId = userId,
            boardId = boardId,
        )

        // 활동 점수 반납
        userClient.updateActivePoint(
            userId = userId,
            activePoint = -ActivePoint.HEART.point
        )
    }

    fun createHeartInComment(commentId: Long, userId: Long): CreateHeartInCommentOuterResponse {
        // 비용 검증 및 차감
        val user = userClient.findById(userId)

        if (user.balance < Balance.HEART.cost){
            throw ExceptionType.withType(MessageType.NOT_ENOUGH_BALANCE)
        }

        userClient.updateBalance(userId, -Balance.HEART.cost)

        // 하트 중복 및 코멘트 존재 여부 검증
        if (commentHeartReader.existsByUserIdAndCommentId(userId, commentId)) {
            throw ExceptionType.withType(MessageType.ALREADY_EXIST)
        }

        if (!commentReader.existsById(commentId)){
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        val commentHeart =
            commentHeartWriter.create(
                userId = userId,
                commentId = commentId,
            )

        // 활동 점수 변경
        userClient.updateActivePoint(
            userId = userId,
            activePoint = ActivePoint.HEART.point
        )

        // 알림 전송
        val comment = commentReader.findById(commentId)

        alarmWriter.create(
            userId = userId,
            targetUserId = comment.userId,
            contentType = ContentType.COMMENT_HEART,
            contentId = commentHeart.commentHeartId,
            alarmStatus = AlarmStatus.CREATED,
        )

        return CreateHeartInCommentOuterResponse(commentHeart.commentHeartId)
    }

    fun deleteHeartInComment(commentId: Long, userId: Long) {
        if (!commentReader.existsById(commentId)){
            throw ExceptionType.withType(MessageType.NOT_EXIST)
        }

        // 하트 삭제
        commentHeartWriter.deleteByUserIdAndCommentId(
            userId = userId,
            commentId = commentId,
        )

        // 활동 점수 반납
        userClient.updateActivePoint(
            userId = userId,
            activePoint = -ActivePoint.HEART.point
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