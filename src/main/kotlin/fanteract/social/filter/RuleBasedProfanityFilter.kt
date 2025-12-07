package fanteract.social.filter

import fanteract.social.dto.FilterResult
import fanteract.social.enumerate.RiskLevel
import org.springframework.stereotype.Component

@Component
class RuleBasedProfanityFilter(
    private val normalizer: TextNormalizer
) {

    // 아주 심한 욕 (BLOCK 대상)
    private val hardProfanityPatterns = listOf(
        "씨발", "씹년", "개새끼", "닥쳐", "뒤져", "죽어라",
        "병신", "병1신", "ㅂㅅ", "ㅄ", "ㅅㅂ"
    ).map { it.toRegex() }

    // 공격적이지만 약한 표현 (WARN 대상)
    private val softProfanityPatterns = listOf(
        "바보", "멍청이", "쓰레기", "재수없", "꺼져"
    ).map { it.toRegex() }

    fun filter(raw: String): FilterResult {
        val text = normalizer.normalize(raw)

        // 하드 욕설: BLOCK
        if (hardProfanityPatterns.any { it.containsMatchIn(text) }) {
            return FilterResult(
                action = RiskLevel.BLOCK,
                reason = "강한 욕설/모욕 감지"
            )
        }

        // 부드러운 욕설: WARN
        if (softProfanityPatterns.any { it.containsMatchIn(text) }) {
            return FilterResult(
                action = RiskLevel.WARN,
                reason = "공격적인 표현 감지"
            )
        }

        return FilterResult(
            action = RiskLevel.ALLOW
        )
    }
}