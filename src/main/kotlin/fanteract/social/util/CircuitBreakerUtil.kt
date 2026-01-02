package fanteract.social.util

import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

@Component
class CircuitBreakerUtil(
    private val manager: CircuitBreakerManager,
) {
    private val nameCache = ConcurrentHashMap<String, String>()

    fun <T> circuitBreaker(
        name: String? = null,
        profile: CircuitBreakerManager.Profile = manager.baseConfig, // config
        block: () -> T,
    ): CircuitBreakerCall<T> {
        // 클래스-메서드-파라미터 형식으로 이름 구축
        val resolvedName = name ?: resolveAutoName(profile)

        val cb = manager.circuitBreaker(resolvedName, profile)
        return CircuitBreakerCall(execute = { cb.executeSupplier(block) })
    }

    // 불러온 클래스 및 메서드 네임 가공
    private fun resolveAutoName(profile: CircuitBreakerManager.Profile): String {
        val caller = findCallerFrame() ?: return "cb-${profile.name}-unknown"

        val classFqcn = caller.className
        val className = classFqcn.substringAfterLast(".")
        val methodName = caller.methodName

        val paramTypes = resolveParamTypes(classFqcn, methodName)
        val paramSig = paramTypes.joinToString(",") { it.substringAfterLast(".") }

        val signatureKey = "${profile.name}|$classFqcn|$methodName|$paramSig|"

        return nameCache.computeIfAbsent(signatureKey) {
            "cb-${profile.name}-$className-$methodName($paramSig)"
        }
    }

    // 스택트레이스로부터 클래스 및 메서드 네임 불러오기
    private fun findCallerFrame(): StackTraceElement? {
        val utilClass = this::class.java.name
        return Throwable().stackTrace.firstOrNull { e ->
            e.className != utilClass && !e.className.startsWith("java.") && !e.className.startsWith("kotlin.")
        }
    }

    // 파라미터 타입 불러오기
    private fun resolveParamTypes(classFqcn: String, methodName: String): List<String> {
        return try {
            val clazz = Class.forName(classFqcn)

            val methods = clazz.declaredMethods.filter { it.name == methodName }
            val best = methods.minByOrNull { it.parameterCount } ?: return emptyList()

            best.parameterTypes.map { it.name }
        } catch (_: Throwable) {
            emptyList()
        }
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
    fun fallback(handler: (Throwable) -> T): CircuitBreakerCall<T> =
        apply {
            this.fallbackAll = handler
        }

    // OPEN 예외에 대해 폴백 메서드 설정
    fun fallbackIfOpen(handler: (CallNotPermittedException) -> T): CircuitBreakerCall<T> =
        apply {
            this.fallbackOpen = handler
        }

    // 명시한 예외에 대해 폴백 메서드 설정
    fun fallbackIf(
        predicate: (Throwable) -> Boolean,
        handler: (Throwable) -> T,
    ): CircuitBreakerCall<T> =
        apply {
            this.conditionalFallbacks += predicate to handler
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
