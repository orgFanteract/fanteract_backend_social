package fanteract.social.dto

import fanteract.social.enumerate.RiskLevel

data class FilterResult(
    val action: RiskLevel,
    val reason: String? = null,
    val score: Double? = null, // ML 토크시티 점수 등
)
