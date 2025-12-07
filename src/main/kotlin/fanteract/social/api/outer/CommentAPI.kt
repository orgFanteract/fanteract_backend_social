package fanteract.social.api.outer

import fanteract.social.service.CommentService
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import fanteract.social.annotation.LoginRequired
import fanteract.social.config.JwtParser
import fanteract.social.dto.outer.*
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/comments")
class CommentAPI(
    private val commentService: CommentService
) {
    // 게시글 코멘트 조회
    //@LoginRequired
    @Operation(summary = "특정 게시글의 코멘트 목록 조회")
    @GetMapping("/{boardId}/board")
    fun readCommentsByBoardId(
        @PathVariable boardId: Long,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
    ): ResponseEntity<ReadCommentPageOuterResponse> {
        val response = commentService.readCommentsByBoardId(
            boardId = boardId,
            page = page,
            size = size
        )
        return ResponseEntity.ok().body(response)
    }

    // 특정 유저의 코멘트 조회
    //@LoginRequired
    @Operation(summary = "사용자가 작성한 코멘트 목록 조회")
    @GetMapping("/user")
    fun readCommentsByUserId(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
    ): ResponseEntity<ReadCommentPageOuterResponse> {
        //val userId = JwtParser.extractKey(request, "userId")
        val response = commentService.readCommentsByUserId(
            userId = userId,
            page = page,
            size = size
        )
        return ResponseEntity.ok().body(response)
    }

    // 코멘트 생성
    //@LoginRequired
    @Operation(summary = "코멘트 생성")
    @PostMapping("/board/{boardId}")
    fun createComment(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable boardId: Long,
        @RequestBody createCommentOuterRequest: CreateCommentOuterRequest,
    ): ResponseEntity<CreateCommentOuterResponse> {
        //val userId = JwtParser.extractKey(request, "userId")
        val response = commentService.createComment(
            boardId = boardId,
            userId = userId,
            createCommentOuterRequest = createCommentOuterRequest
        )
        return ResponseEntity.ok().body(response)
    }

    // 코멘트 수정
    //@LoginRequired
    @Operation(summary = "코멘트 수정")
    @PutMapping("/{commentId}")
    fun updateComment(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable commentId: Long,
        @RequestBody updateCommentOuterRequest: UpdateCommentOuterRequest,
    ): ResponseEntity<Void> {
        //val userId = JwtParser.extractKey(request, "userId")
        commentService.updateComment(
            commentId = commentId,
            userId = userId,
            updateCommentOuterRequest = updateCommentOuterRequest
        )
        return ResponseEntity.ok().build()
    }

    // 코멘트 삭제
    //@LoginRequired
    @Operation(summary = "코멘트 삭제")
    @DeleteMapping("/{commentId}")
    fun deleteComment(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable commentId: Long,
    ): ResponseEntity<Void> {
        //val userId = JwtParser.extractKey(request, "userId")
        commentService.deleteComment(
            commentId = commentId,
            userId = userId
        )
        return ResponseEntity.ok().build()
    }

    // 게시글 좋아요 선택
    //@LoginRequired
    @Operation(summary = "코멘트 좋아요 생성")
    @PostMapping("/{commentId}/heart")
    fun createHeartInComment(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable commentId: Long,
    ): ResponseEntity<CreateHeartInCommentOuterResponse> {
        //val userId = JwtParser.extractKey(request, "userId")
        val response = commentService.createHeartInComment(commentId, userId)

        return ResponseEntity.ok().body(response)
    }

    // 게시글 좋아요 취소
    //@LoginRequired
    @Operation(summary = "코멘트 좋아요 해제")
    @DeleteMapping("/{commentId}/heart")
    fun deleteHeartInComment(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable commentId: Long,
    ): ResponseEntity<Void> {
        //val userId = JwtParser.extractKey(request, "userId")
        commentService.deleteHeartInComment(commentId, userId)

        return ResponseEntity.ok().build()
    }
}