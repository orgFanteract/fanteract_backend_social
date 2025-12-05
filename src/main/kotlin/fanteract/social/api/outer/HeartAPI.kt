package fanteract.social.api.outer

import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import fanteract.social.annotation.LoginRequired
import fanteract.social.config.JwtParser
import fanteract.social.dto.outer.*
import fanteract.social.service.HeartService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

class HeartAPI(
    private val heartService: HeartService,
) {
    @LoginRequired
    @Operation(summary = "게시글 좋아요 생성")
    @PostMapping("/{boardId}/heart")
    fun createHeartInBoard(
        request: HttpServletRequest,
        @PathVariable boardId: Long,
    ): ResponseEntity<CreateHeartInBoardOuterResponse> {
        val userId = JwtParser.extractKey(request, "userId")
        val response = heartService.createHeartInBoard(boardId, userId)

        return ResponseEntity.ok().body(response)
    }

    // 게시글 좋아요 취소
    @LoginRequired
    @Operation(summary = "게시글 좋아요 해제")
    @DeleteMapping("/{boardId}/heart")
    fun deleteHeartInBoard(
        request: HttpServletRequest,
        @PathVariable boardId: Long,
    ): ResponseEntity<Void> {
        val userId = JwtParser.extractKey(request, "userId")
        heartService.deleteHeartInBoard(boardId, userId)

        return ResponseEntity.ok().build()
    }

    // 게시글 좋아요 선택
    @LoginRequired
    @Operation(summary = "코멘트 좋아요 생성")
    @PostMapping("/{commentId}/heart")
    fun createHeartInComment(
        request: HttpServletRequest,
        @PathVariable commentId: Long,
    ): ResponseEntity<CreateHeartInCommentOuterResponse> {
        val userId = JwtParser.extractKey(request, "userId")
        val response = heartService.createHeartInComment(commentId, userId)

        return ResponseEntity.ok().body(response)
    }

    // 게시글 좋아요 취소
    @LoginRequired
    @Operation(summary = "코멘트 좋아요 해제")
    @DeleteMapping("/{commentId}/heart")
    fun deleteHeartInComment(
        request: HttpServletRequest,
        @PathVariable commentId: Long,
    ): ResponseEntity<Void> {
        val userId = JwtParser.extractKey(request, "userId")
        heartService.deleteHeartInComment(commentId, userId)

        return ResponseEntity.ok().build()
    }
}