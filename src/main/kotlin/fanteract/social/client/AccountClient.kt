package fanteract.social.client

import fanteract.social.dto.client.*
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import fanteract.social.util.CircuitBreakerManager
import fanteract.social.util.CircuitBreakerUtil
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClient

@Component
class AccountClient(
    @Value("\${client.account-service.url}") userServiceUrl: String,
    private val restClient: RestClient = RestClient.builder()
        .baseUrl(userServiceUrl)
        .build(),
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    private val circuitBreakerUtil: CircuitBreakerUtil,
    private val circuitBreakerManager: CircuitBreakerManager,
) {

    //@circuitBreaker(name = "accountClient", fallbackMethod = "updateAbusePointFallback")
    fun updateAbusePoint(userId: Long, abusePoint: Int) {
        val request = UpdateAbusePointInnerRequest(abusePoint = abusePoint)

        restClient.put()
            .uri("/internal/users/{userId}/abuse-point", userId)
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    //@circuitBreaker(name = "accountClient", fallbackMethod = "existsByIdFallback")
    fun existsById(userId: Long): Boolean {
        val response = restClient.get()
            .uri("/internal/users/{userId}/exists", userId)
            .retrieve()
            .body(ReadUserExistsInnerResponse::class.java)

        return response?.exists ?: false
    }

    //@circuitBreaker(name = "accountClient", fallbackMethod = "findByIdFallback")
    fun findById(userId: Long): ReadUserInnerResponse {
        val response = restClient.get()
            .uri("/internal/users/{userId}", userId)
            .retrieve()
            .body(ReadUserInnerResponse::class.java)

        return requireNotNull(response) { "User not found for id=$userId" }
    }

    //@circuitBreaker(name = "accountClient", fallbackMethod = "updateBalanceFallback")
    fun updateBalance(userId: Long, balance: Int) {
        val request = UpdateBalanceInnerRequest(balance = balance)

        restClient.put()
            .uri("/internal/users/{userId}/balance", userId)
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    //@circuitBreaker(name = "accountClient", fallbackMethod = "updateActivePointFallback")
    fun updateActivePoint(userId: Long, activePoint: Int) {
        val request = UpdateActivePointInnerRequest(activePoint = activePoint)

        restClient.put()
            .uri("/internal/users/{userId}/active-point", userId)
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    //@circuitBreaker(name = "accountClient", fallbackMethod = "findByIdInFallback")
    fun findByIdIn(userIds: List<Long>): List<ReadUserInnerResponse> {
        val response = restClient.get()
            .uri { builder ->
                builder
                    .path("/internal/users/batch")
                    .queryParam("userIds", *userIds.toTypedArray())
                    .build()
            }
            .retrieve()
            .body(ReadUserListInnerResponse::class.java)

        return response?.users ?: emptyList()
    }

    //@circuitBreaker(name = "accountClient", fallbackMethod = "debitBalanceIfEnoughFallback")
    fun debitBalanceIfEnough1(userId: Long, cost: Int): UpdateUserDebitIfEnoughInnerResponse {
        val request = UpdateUserDebitIfEnoughInnerRequest(amount = cost)

        val response = restClient.put()
            .uri("/internal/users/{userId}/debit", userId)
            .body(request)
            .retrieve()
            .body(UpdateUserDebitIfEnoughInnerResponse::class.java)

        return requireNotNull(response) { "debitIfEnough response is null for userId=$userId" }
    }

    // test
    fun debitBalanceIfEnough2(
        userId: Long,
        cost: Int
    ): UpdateUserDebitIfEnoughInnerResponse {
        val request = UpdateUserDebitIfEnoughInnerRequest(amount = cost)

        val response =
            circuitBreakerUtil.circuitBreaker(
                name = "debitBalance",
                profile = circuitBreakerManager.accountConfig
            ){
                restClient.put()
                    .uri("/internal/users/{userId}/debit", userId)
                    .body(request)
                    .retrieve()
                    .body(UpdateUserDebitIfEnoughInnerResponse::class.java)
            }.fallbackIf({ ex -> ex is NoSuchElementException }) {
                throw ExceptionType.withType(MessageType.NOT_EXIST)
            }.fallbackIfOpen {
                throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
            }.fallback{
                throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
            }.get()

        return requireNotNull(response) { "debitIfEnough response is null for userId=$userId" }
    }

    // ===== fallback methods (메서드별 이름 분리) =====

    @Suppress("unused")
    private fun updateAbusePointFallback(userId: Long, abusePoint: Int, ex: Throwable) {
        returnStatus("accountClient", ex)
        throw ExceptionType.withType(MessageType.INVALID_CONNECTED_SERVICE)
    }

    @Suppress("unused")
    private fun existsByIdFallback(userId: Long, ex: Throwable): Boolean {
        returnStatus("accountClient",  ex)
        throw ExceptionType.withType(MessageType.INVALID_CONNECTED_SERVICE)
    }

    @Suppress("unused")
    private fun findByIdFallback(userId: Long, ex: Throwable): ReadUserInnerResponse? {
        returnStatus("accountClient", ex)
        throw ExceptionType.withType(MessageType.INVALID_CONNECTED_SERVICE)
    }

    @Suppress("unused")
    private fun updateBalanceFallback(userId: Long, balance: Int, ex: Throwable) {
        returnStatus("accountClient", ex)
        throw ExceptionType.withType(MessageType.INVALID_CONNECTED_SERVICE)
    }

    @Suppress("unused")
    private fun updateActivePointFallback(userId: Long, activePoint: Int, ex: Throwable) {
        returnStatus("accountClient", ex)
        throw ExceptionType.withType(MessageType.INVALID_CONNECTED_SERVICE)
    }

    @Suppress("unused")
    private fun findByIdInFallback(userIds: List<Long>, ex: Throwable): List<ReadUserInnerResponse>{
        returnStatus("accountClient", ex)
        throw ExceptionType.withType(MessageType.INVALID_CONNECTED_SERVICE)
    }

    @Suppress("unused")
    private fun debitBalanceIfEnoughFallback(userId: Long, cost: Int, ex: Throwable): UpdateUserDebitIfEnoughInnerResponse {
        returnStatus("accountClient", ex)
        throw ExceptionType.withType(MessageType.INVALID_CONNECTED_SERVICE)
    }

    private fun returnStatus(client: String, ex: Throwable) {
        val cb = circuitBreakerRegistry.circuitBreaker(client)
        val state = cb.state
        println("fallback client=$client,ex=${ex::class.qualifiedName}:${ex.message}, state=$state")
    }


}