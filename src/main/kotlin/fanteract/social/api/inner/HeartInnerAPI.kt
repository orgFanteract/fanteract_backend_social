package fanteract.social.api.inner

import fanteract.social.dto.inner.*
import fanteract.social.entity.CommentHeart
import fanteract.social.service.HeartService
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
@RequestMapping("/internal/hearts")
class HeartInnerAPI(
    private val heartService: HeartService,
) {
    /** 게시글 목록에 대한 좋아요 정보 조회 */
    @GetMapping("/boards")
    fun findBoardHeartByBoardIdIn(
        @RequestParam("boardIds") idList: List<Long>,
    ): ResponseEntity<BoardHeartListInnerResponse> {
        val response = heartService.findBoardHeartByBoardIdIn(idList)
        return ResponseEntity.ok().body(response)
    }

    /** 특정 유저가 특정 게시글에 좋아요 했는지 여부 */
    @GetMapping("/boards/exists")
    fun existsBoardHeartByUserIdAndBoardId(
        @RequestParam userId: Long,
        @RequestParam boardId: Long,
    ): ResponseEntity<Boolean> {
        val response = heartService.existsBoardHeartByUserIdAndBoardId(userId, boardId)
        return ResponseEntity.ok().body(response)
    }

    // 코멘트 하트 기능

    /** 댓글 목록에 대한 좋아요 정보 조회 */
    @GetMapping("/comments")
    fun findCommentHeartByCommentIdIn(
        @RequestParam("commentIds") idList: List<Long>,
    ): ResponseEntity<List<CommentHeart>> {
        val response = heartService.findByCommentIdIn(idList)
        return ResponseEntity.ok().body(response)
    }

    /** 특정 유저가 특정 댓글에 좋아요 했는지 여부 */
    @GetMapping("/comments/exists")
    fun existsCommentHeartByUserIdAndCommentId(
        @RequestParam userId: Long,
        @RequestParam commentId: Long,
    ): ResponseEntity<Boolean> {
        val response = heartService.existsCommentHeartByUserIdAndCommentId(userId, commentId)
        return ResponseEntity.ok().body(response)
    }
}
