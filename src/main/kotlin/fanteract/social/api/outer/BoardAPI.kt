package fanteract.social.api.outer

import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import fanteract.social.annotation.LoginRequired
import fanteract.social.config.JwtParser
import fanteract.social.dto.outer.*
import fanteract.social.service.BoardService
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
@RequestMapping("/api/boards")
class BoardAPI(
    private val boardService: BoardService,
) {
    //@LoginRequired
    @Operation(summary = "게시글 생성")
    @PostMapping()
    fun createBoard(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestBody createBoardOuterRequest: CreateBoardOuterRequest,
    ): ResponseEntity<CreateBoardOuterResponse> {
        
        val response = boardService.createBoard(createBoardOuterRequest, userId)

        return ResponseEntity.ok().body(response)
    }

    // 사용자 작성 게시글 조회
    //@LoginRequired
    @Operation(summary = "사용자 소유 게시글 조회")
    @GetMapping("/user")
    fun readBoardByUserId(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
    ): ResponseEntity<ReadBoardListOuterResponse> {
        
        val response = boardService.readBoardByUserId(page, size, userId)

        return ResponseEntity.ok().body(response)
    }

    // 전체 게시글 조회
    //@LoginRequired
    @Operation(summary = "전체 게시글 조회")
    @GetMapping("")
    fun readBoard(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
    ): ResponseEntity<ReadBoardListOuterResponse> {
        val response = boardService.readBoard(
            page = page,
            size = size
        )

        return ResponseEntity.ok().body(response)
    }

    // 특정 게시글 상세 조회
    //@LoginRequired
    @Operation(summary = "특정 게시글 상세 조회")
    @GetMapping("/{boardId}/board")
    fun readBoardDetail(
        @PathVariable boardId: Long,
    ): ResponseEntity<ReadBoardDetailOuterResponse> {
        val response = boardService.readBoardDetail(boardId)

        return ResponseEntity.ok().body(response)
    }

    // 게시글 수정
    //@LoginRequired
    @Operation(summary = "게시글 수정")
    @PutMapping("/{boardId}")
    fun updateBoard(
        @RequestHeader("X-User-Id") userId: Long,
        @PathVariable boardId: Long,
        @RequestBody updateBoardOuterRequest: UpdateBoardOuterRequest
    ): ResponseEntity<Void> {
        
        boardService.updateBoard(boardId, userId, updateBoardOuterRequest)

        return ResponseEntity.ok().build()
    }
}