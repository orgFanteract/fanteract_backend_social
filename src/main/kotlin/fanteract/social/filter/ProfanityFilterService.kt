package fanteract.social.filter

import fanteract.social.client.AccountClient
import fanteract.social.dto.FilterResult
import fanteract.social.enumerate.RiskLevel
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class ProfanityFilterService(
    private val ruleBasedFilter: RuleBasedProfanityFilter,
    private val mlToxicityClient: MlToxicityClient,
    private val accountClient: AccountClient,
    // private val userWriter: UserWriter,
) {
    private val log = KotlinLogging.logger {}

    fun filter(text: String): FilterResult {
        // 1단계: 룰 기반 필터
        val ruleResult = ruleBasedFilter.filter(text)

        if (ruleResult.action == RiskLevel.BLOCK) {
            return ruleResult
        } else if (ruleResult.action == RiskLevel.WARN) {
            return ruleResult
        }

        // 규칙 상 괜찮거나(WARN 이하) 애매하면 ML 호출
        val score = mlToxicityClient.getToxicityScore(text)

        return when {
            score >= 0.9 ->
                FilterResult(
                    action = RiskLevel.BLOCK,
                    reason = "ML: 고위험 공격성 감지",
                    score = score,
                )

            score >= 0.6 ->
                FilterResult(
                    action = RiskLevel.WARN,
                    reason = "ML: 중간 수준 공격성 감지",
                    score = score,
                )

            else ->
                FilterResult(
                    action = RiskLevel.ALLOW,
                    score = score,
                )
        }
    }

    fun checkProfanityAndUpdateAbusePoint(
        userId: Long,
        text: String,
    ): RiskLevel {
        val filterResult = filter(text)

        when (filterResult.action) {
            RiskLevel.BLOCK -> {
                accountClient.updateAbusePoint(
                    userId = userId,
                    abusePoint = 10,
                )

                log.warn { "부적절한 표현이 포함되어 부정 점수가 10점 적용되었습니다" }
            }

            RiskLevel.WARN -> {
                accountClient.updateAbusePoint(
                    userId = userId,
                    abusePoint = 5,
                )

                log.warn { "부적절한 표현이 포함되어 부정 점수가 5점 적용되었습니다. 게시는 진행됩니다." }
            }

            else -> {
            }
        }

        return filterResult.action
    }
}
