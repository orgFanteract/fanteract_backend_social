package fanteract.social.dto.client

import fanteract.social.enumerate.RiskLevel
import java.time.LocalDateTime


data class ReadUserExistsInnerResponse(
    val exists: Boolean,
)

data class UpdateBalanceInnerRequest(
    val balance: Int,
)

data class UpdateActivePointInnerRequest(
    val activePoint: Int,
)

data class UpdateAbusePointInnerRequest(
    val abusePoint: Int,
)

data class ReadUserInnerResponse(
    val userId: Long = 0L,
    val email: String,
    val password: String,
    val name: String,
    var balance: Int = 0,
    var activePoint: Int = 0,
    var abusePoint: Int = 0,
    val passExpiredAt: LocalDateTime? = null,
)

data class ReadUserListInnerResponse(
    val users: List<ReadUserInnerResponse>
)
