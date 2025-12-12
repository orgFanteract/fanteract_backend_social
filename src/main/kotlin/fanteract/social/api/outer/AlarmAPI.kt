package fanteract.social.api.outer

import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import fanteract.social.annotation.LoginRequired
import fanteract.social.config.JwtParser
import fanteract.social.dto.outer.ReadAlarmListOuterResponse
import fanteract.social.service.AlarmService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/alarms")
class AlarmAPI(
    private val alarmService: AlarmService,
) {
    
    @Operation(summary = "사용자별 알람 조회")
    @GetMapping()
    fun readAlarmByUserId(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("size", defaultValue = "10") size: Int,
    ): ResponseEntity<ReadAlarmListOuterResponse> {
        val response = alarmService.readAlarmByUserId(userId, page, size)

        return ResponseEntity.ok().body(response)
    }
}