package fanteract.social.api.outer

import io.swagger.v3.oas.annotations.Operation
import fanteract.social.dto.outer.*
import fanteract.social.service.HeartService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/hearts")
class HeartAPI(
    private val heartService: HeartService,
) {
    
    @Operation(summary = "게시글 좋아요 생성")
    @PostMapping("/{boardId}/board")
    fun createHeartInBoard(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable boardId: Long,
    ): ResponseEntity<CreateHeartInBoardOuterResponse> {
        
        val response = heartService.createHeartInBoard(boardId, userId)

        return ResponseEntity.ok().body(response)
    }

    // 게시글 좋아요 취소
    
    @Operation(summary = "게시글 좋아요 해제")
    @DeleteMapping("/{boardId}/board")
    fun deleteHeartInBoard(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable boardId: Long,
    ): ResponseEntity<Void> {
        
        heartService.deleteHeartInBoard(boardId, userId)

        return ResponseEntity.ok().build()
    }

    // 게시글 좋아요 선택
    @Operation(summary = "코멘트 좋아요 생성")
    @PostMapping("/{commentId}/comment")
    fun createHeartInComment(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable commentId: Long,
    ): ResponseEntity<CreateHeartInCommentOuterResponse> {
        val response = heartService.createHeartInComment(commentId, userId)

        return ResponseEntity.ok().body(response)
    }

    // 게시글 좋아요 취소
    
    @Operation(summary = "코멘트 좋아요 해제")
    @DeleteMapping("/{commentId}/comment")
    fun deleteHeartInComment(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable commentId: Long,
    ): ResponseEntity<Void> {
        heartService.deleteHeartInComment(commentId, userId)

        return ResponseEntity.ok().build()
    }
}