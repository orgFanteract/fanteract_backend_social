package fanteract.social.util

import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CircuitBreakerUtil(
    private val manager: CircuitBreakerManager,
) {
    fun <T> circuitBreaker(
        name: String = UUID.randomUUID().toString(), // key
        profile: CircuitBreakerManager.Profile = manager.baseConfig, // config
        block: () -> T
    ): CircuitBreakerCall<T> {
        val cb = manager.circuitBreaker(name, profile)
        return CircuitBreakerCall(execute = { cb.executeSupplier(block) })
    }
}

class CircuitBreakerCall<T>(
    private val execute: () -> T,
) {
    private var fallbackAll: ((Throwable) -> T)? = null
    private var fallbackOpen: ((CallNotPermittedException) -> T)? = null
    private val conditionalFallbacks = mutableListOf<Pair<(Throwable) -> Boolean, (Throwable) -> T>>()
    private val ignoredHandlers = mutableListOf<Pair<(Throwable) -> Boolean, (Throwable) -> T>>()

    // 모든 예외에 대한 폴백 메서드 설정
    fun fallback(handler: (Throwable) -> T): CircuitBreakerCall<T> = apply {
        this.fallbackAll = handler
    }

    // OPEN 예외에 대해 폴백 메서드 설정
    fun fallbackIfOpen(handler: (CallNotPermittedException) -> T): CircuitBreakerCall<T> = apply {
        this.fallbackOpen = handler
    }

    // 명시한 예외에 대해 폴백 메서드 설정
    fun fallbackIf(
        predicate: (Throwable) -> Boolean,
        handler: (Throwable) -> T,
    ): CircuitBreakerCall<T> = apply {
        this.conditionalFallbacks += predicate to handler
    }

    // deprecated
    // 명시한 예외에 대해 무시(상태 변화에 영향을 주지 않음)
    fun ignoreIf(
        predicate: (Throwable) -> Boolean,
        handler: (Throwable) -> T,
    ): CircuitBreakerCall<T> = apply {
        this.ignoredHandlers += predicate to handler
    }

    // 결과 반환
    fun get(): T {
        return try {
            // 결과 반환
            execute()
        } catch (ex: CallNotPermittedException) {
            // OPEN 예외에 대해 fallbackIfOpen 메서드 실행
            fallbackOpen?.invoke(ex) ?: throw ex
        } catch (ex: Throwable) {
            // 명시한 예외에 대해 ignoreIf 메서드 실행
            for ((pred, handler) in ignoredHandlers) {
                if (pred(ex)) {
                    return handler(ex)
                }
            }

            // 명시한 예외에 대해 fallbackIf 메서드 실행
            for ((pred, handler) in conditionalFallbacks) {
                if (pred(ex)) {
                    return handler(ex)
                }
            }

            // 이외 예외에 대해 fallback 메서드 실행
            fallbackAll?.let {
                return it(ex)
            }

            throw ex
        }
    }
}