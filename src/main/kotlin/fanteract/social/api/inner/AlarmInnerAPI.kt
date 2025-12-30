package fanteract.social.api.inner

import fanteract.social.annotation.LoginRequired
import fanteract.social.config.JwtParser
import fanteract.social.dto.inner.CreateAlarmInnerRequest
import fanteract.social.dto.inner.CreateAlarmInnerResponse
import fanteract.social.service.AlarmService
import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Hidden
@RestController
@RequestMapping("/internal/alarms")
class AlarmInnerAPI(
    private val alarmService: AlarmService,
) {
    @PostMapping()
    @Operation(summary = "알람 생성")
    fun create(
        @RequestHeader("X-User-Id") userId: Long,
        @RequestBody createAlarmInnerRequest: CreateAlarmInnerRequest,
    ): ResponseEntity<CreateAlarmInnerResponse> {
        val response =
            alarmService.create(
                createAlarmInnerRequest = createAlarmInnerRequest,
                userId = userId,
            )

        return ResponseEntity.ok().body(response)
    }
}
