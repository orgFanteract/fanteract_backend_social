package fanteract.social.client

import fanteract.social.dto.client.*
import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import fanteract.social.util.CircuitBreakerManager
import fanteract.social.util.CircuitBreakerUtil
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

// 환경 변수에 존재하는 매핑 데이터가 그대로 입력되면서 잘못된 url이 적용됨. 변경 요망

@Component
class AccountClient(
    @Value("\${client.account-service.url}")
    accountServiceUrl: String,
    restClientBuilder: RestClient.Builder,
    private val circuitBreakerUtil: CircuitBreakerUtil,
    private val circuitBreakerManager: CircuitBreakerManager,
) {
    private val restClient: RestClient = restClientBuilder
        .baseUrl(accountServiceUrl)
        .build()

    fun updateAbusePoint(
        userId: Long,
        abusePoint: Int,
    ) {
        val request = UpdateAbusePointInnerRequest(abusePoint = abusePoint)
        circuitBreakerUtil
            .circuitBreaker(
                profile = circuitBreakerManager.accountConfig,
            ) {
                restClient
                    .put()
                    .uri("/internal/users/{userId}/abuse-point", userId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity()
            }.fallbackIfOpen {
                throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
            }.fallback {
                throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
            }.get()
    }

    fun existsById(userId: Long): Boolean {
        val response =
            circuitBreakerUtil
                .circuitBreaker(
                    profile = circuitBreakerManager.accountConfig,
                ) {
                    restClient
                        .get()
                        .uri("/internal/users/{userId}/exists", userId)
                        .retrieve()
                        .body(ReadUserExistsInnerResponse::class.java)
                }.fallbackIfOpen {
                    throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
                }.fallback {
                    throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
                }.get()

        return response?.exists ?: false
    }

    fun findById(userId: Long): ReadUserInnerResponse {
        val response =
            circuitBreakerUtil
                .circuitBreaker(
                    profile = circuitBreakerManager.accountConfig,
                ) {
                    restClient
                        .get()
                        .uri("/internal/users/{userId}", userId)
                        .retrieve()
                        .body(ReadUserInnerResponse::class.java)
                }.fallbackIfOpen {
                    throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
                }.fallback {
                    throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
                }.get()

        return requireNotNull(response) { "User not found for id=$userId" }
    }

    fun updateBalance(
        userId: Long,
        balance: Int,
    ) {
        val request = UpdateBalanceInnerRequest(balance = balance)

        circuitBreakerUtil
            .circuitBreaker(
                profile = circuitBreakerManager.accountConfig,
            ) {
                restClient
                    .put()
                    .uri("/internal/users/{userId}/balance", userId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity()
            }.fallbackIfOpen {
                throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
            }.fallback {
                throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
            }.get()
    }

    fun updateActivePoint(
        userId: Long,
        activePoint: Int,
    ) {
        val request = UpdateActivePointInnerRequest(activePoint = activePoint)

        circuitBreakerUtil
            .circuitBreaker(
                profile = circuitBreakerManager.accountConfig,
            ) {
                restClient
                    .put()
                    .uri("/internal/users/{userId}/active-point", userId)
                    .body(request)
                    .retrieve()
                    .toBodilessEntity()
            }.fallbackIfOpen {
                throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
            }.fallback {
                throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
            }.get()
    }

    fun findByIdIn(userIds: List<Long>): List<ReadUserInnerResponse> {
        val response =
            circuitBreakerUtil
                .circuitBreaker(
                    profile = circuitBreakerManager.accountConfig,
                ) {
                    restClient
                        .get()
                        .uri { builder ->
                            builder
                                .path("/internal/users/batch")
                                .queryParam("userIds", *userIds.toTypedArray())
                                .build()
                        }.retrieve()
                        .body(ReadUserListInnerResponse::class.java)
                }.fallbackIfOpen {
                    throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
                }.fallback {
                    throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
                }.get()

        return response?.users ?: emptyList()
    }

    fun debitBalanceIfEnough(
        userId: Long,
        cost: Int,
    ): UpdateUserDebitIfEnoughInnerResponse {
        val request = UpdateUserDebitIfEnoughInnerRequest(amount = cost)

        val response =
            circuitBreakerUtil
                .circuitBreaker(
                    profile = circuitBreakerManager.accountConfig,
                ) {
                    restClient
                        .put()
                        .uri("/internal/users/{userId}/debit", userId)
                        .body(request)
                        .retrieve()
                        .body(UpdateUserDebitIfEnoughInnerResponse::class.java)
                }.fallbackIfOpen {
                    throw ExceptionType.withType(MessageType.CALL_NOT_PERMITTED)
                }.fallback {
                    throw ExceptionType.withType(MessageType.INVALID_ACCESS_RESOURCE)
                }.get()

        return requireNotNull(response) { "debitIfEnough response is null for userId=$userId" }
    }
}
