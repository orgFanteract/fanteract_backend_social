package fanteract.social.util

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
class CircuitBreakerManager(
    private val registry: CircuitBreakerRegistry,
) {
    // 서킷브레이커 설정 템플릿
    open class Profile(
        val name: String,
        val parent: Profile? = null,

        val slidingWindowSize: Int? = null,
        val minimumNumberOfCalls: Int? = null,
        val failureRateThreshold: Float? = null,
        val waitDurationInOpenState: Duration? = null,
        val permittedNumberOfCallsInHalfOpenState: Int? = null,

        val recordExceptions: Set<Class<out Throwable>>? = null,
        val ignoreExceptions: Set<Class<out Throwable>>? = null,
    )

    // 기본 서킷브레이커 설정
    val baseConfig =
        Profile(
            name = "baseConfig",
            slidingWindowSize = 20,
            minimumNumberOfCalls = 6,
            failureRateThreshold = 50f,
            waitDurationInOpenState = Duration.ofSeconds(10),
            permittedNumberOfCallsInHalfOpenState = 5,
            recordExceptions = setOf(
                java.io.IOException::class.java,
                org.springframework.web.client.ResourceAccessException::class.java,
                org.springframework.web.client.RestClientResponseException::class.java,
            ),
            ignoreExceptions = emptySet(),
        )

    // account 서비스용 설정 (baseConfig 상속)
    val accountConfig =
        Profile(
            name = "accountClient",
            parent = baseConfig,
            failureRateThreshold = 40f, // customize
        )

    // Profile → CircuitBreakerConfig 캐시
    private val configCache = ConcurrentHashMap<String, CircuitBreakerConfig>()

    // 이름(name)과 설정(profile)으로 CircuitBreaker 생성
    fun circuitBreaker(
        name: String,
        profile: Profile,
    ) = registry.circuitBreaker(name, resolveConfig(profile))

    // Profile을 CircuitBreakerConfig로 변환
    private fun resolveConfig(profile: Profile): CircuitBreakerConfig {
        return configCache.computeIfAbsent(profile.name) {
            val resolved = resolveMerged(profile)

            CircuitBreakerConfig.custom()
                .slidingWindowSize(resolved.slidingWindowSize!!)
                .minimumNumberOfCalls(resolved.minimumNumberOfCalls!!)
                .failureRateThreshold(resolved.failureRateThreshold!!)
                .waitDurationInOpenState(resolved.waitDurationInOpenState!!)
                .permittedNumberOfCallsInHalfOpenState(resolved.permittedNumberOfCallsInHalfOpenState!!)
                .apply {
                    resolved.recordExceptions?.let { recordExceptions(*it.toTypedArray()) }
                    resolved.ignoreExceptions?.let { ignoreExceptions(*it.toTypedArray()) }
                }
                .build()
        }
    }

    // 부모 → 자식 순으로 설정 병합
    private fun resolveMerged(profile: Profile): Profile {
        val p = profile.parent?.let { resolveMerged(it) }

        return Profile(
            name = profile.name,
            parent = null,

            slidingWindowSize = profile.slidingWindowSize ?: p?.slidingWindowSize,
            minimumNumberOfCalls = profile.minimumNumberOfCalls ?: p?.minimumNumberOfCalls,
            failureRateThreshold = profile.failureRateThreshold ?: p?.failureRateThreshold,
            waitDurationInOpenState = profile.waitDurationInOpenState ?: p?.waitDurationInOpenState,
            permittedNumberOfCallsInHalfOpenState =
                profile.permittedNumberOfCallsInHalfOpenState ?: p?.permittedNumberOfCallsInHalfOpenState,

            recordExceptions = profile.recordExceptions ?: p?.recordExceptions,
            ignoreExceptions = profile.ignoreExceptions ?: p?.ignoreExceptions,
        )
    }
}