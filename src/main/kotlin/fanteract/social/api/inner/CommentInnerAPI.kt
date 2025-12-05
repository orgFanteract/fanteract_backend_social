package fanteract.social.api.inner

import fanteract.social.dto.inner.*
import fanteract.social.enumerate.RiskLevel
import fanteract.social.service.CommentService
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
@Hidden
@RestController
@RequestMapping("/internal/comments")
class CommentInnerAPI(
    private val commentService: CommentService,
) {
    @GetMapping("/{userId}/user/count")
    fun readCountByUserId(
        @PathVariable userId: Long,
    ): ResponseEntity<ReadCommentCountInnerResponse> {
        val response = commentService.countByUserId(userId)

        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/{userId}/user-risk/count")
    fun readCountByUserIdAndRiskLevel(
        @PathVariable userId: Long,
        @RequestParam riskLevel: RiskLevel,
    ): ResponseEntity<ReadCommentCountInnerResponse> {
        val response = commentService.countByUserIdAndRiskLevel(userId, riskLevel)

        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/{userId}/user")
    fun readCountByUserIdAndRiskLevel(
        @PathVariable userId: Long,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam riskLevel: RiskLevel,
    ): ResponseEntity<ReadCommentPageInnerResponse> {
        val response = commentService.findByUserIdAndRiskLevel(page, size, userId, riskLevel)

        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/{commentId}/exists")
    fun readExistsById(
        @PathVariable commentId: Long,
    ): ResponseEntity<ReadCommentExistsInnerResponse>{
        val response = commentService.existsById(commentId)

        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/{commentId}")
    fun readById(
        @PathVariable commentId: Long,
    ): ResponseEntity<ReadCommentDetailInnerResponse>{
        val response = commentService.findById(commentId)

        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/boards")
    fun readByBoardIdIn(
        @RequestParam("boardIds") boardIds: List<Long>,
    ): ResponseEntity<ReadCommentListInnerResponse> {
        val response = commentService.findByBoardIdIn(boardIds)
        return ResponseEntity.ok().body(response)
    }
}