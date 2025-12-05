package fanteract.social.api.inner

import fanteract.social.dto.inner.*
import fanteract.social.enumerate.RiskLevel
import fanteract.social.service.BoardService
import io.swagger.v3.oas.annotations.Hidden
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
@Hidden
@RestController
@RequestMapping("/internal/boards")
class BoardInnerAPI(
    private val boardService: BoardService,
) {
    @GetMapping("/{userId}/user/count")
    fun readBoardCountByUserId(
        @PathVariable userId: Long,
    ): ResponseEntity<ReadBoardCountInnerResponse> {
        val response = boardService.countByUserId(userId)

        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/{userId}/user-risk/count")
    fun readBoardCountByUserIdAndRiskLevel(
        @PathVariable userId: Long,
        @RequestParam riskLevel: RiskLevel,
    ): ResponseEntity<ReadBoardCountInnerResponse> {
        val response = boardService.countByUserIdAndRiskLevel(userId, riskLevel)

        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/{userId}/user")
    fun readBoardByUserIdAndRiskLevel(
        @PathVariable userId: Long,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
        @RequestParam riskLevel: RiskLevel,
    ): ResponseEntity<ReadBoardPageInnerResponse> {
        val response = boardService.findByUserIdAndRiskLevel(page, size, userId, riskLevel)

        return ResponseEntity.ok().body(response)
    }

    @GetMapping("/{boardId}/exists")
    fun readBoardExistsById(
        @PathVariable boardId: Long,
    ): ResponseEntity<ReadBoardExistsInnerResponse> {
        val response = boardService.existsById(boardId)
        return ResponseEntity.ok().body(ReadBoardExistsInnerResponse(isExist = response.isExist))
    }

    @GetMapping("/{boardId}")
    fun readBoardDetailById(
        @PathVariable boardId: Long,
    ): ResponseEntity<ReadBoardDetailInnerResponse> {
        val response = boardService.findById(boardId)
        return ResponseEntity.ok().body(response)
    }
}