package fanteract.social.client

import fanteract.social.dto.client.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class UserClient(
    @Value("\${client.account-service.url}") userServiceUrl: String,
    private val restClient: RestClient = RestClient.builder()
        .baseUrl(userServiceUrl)
        .build(),
) {

    /**
     * 유저 부정 포인트 수정
     * PUT /internal/users/{userId}/abuse-point
     */
    fun updateAbusePoint(userId: Long, abusePoint: Int) {
        val request = UpdateAbusePointInnerRequest(abusePoint = abusePoint)

        restClient.put()
            .uri("/internal/users/{userId}/abuse-point", userId)
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    /**
     * 유저 존재 여부 확인
     * GET /internal/users/{userId}/exists
     */
    fun existsById(userId: Long): Boolean {
        val response = restClient.get()
            .uri("/internal/users/{userId}/exists", userId)
            .retrieve()
            .body(ReadUserExistsInnerResponse::class.java)

        return response?.exists ?: false
    }

    /**
     * 단건 유저 조회
     * GET /internal/users/{userId}
     */
    fun findById(userId: Long): ReadUserInnerResponse {
        val response = restClient.get()
            .uri("/internal/users/{userId}", userId)
            .retrieve()
            .body(ReadUserInnerResponse::class.java)

        return requireNotNull(response) { "User not found for id=$userId" }
    }

    /**
     * 유저 잔액 수정
     * PUT /internal/users/{userId}/balance
     */
    fun updateBalance(userId: Long, balance: Int) {
        val request = UpdateBalanceInnerRequest(balance = balance)

        restClient.put()
            .uri("/internal/users/{userId}/balance", userId)
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    /**
     * 유저 사용 가능 포인트 수정
     * PUT /internal/users/{userId}/active-point
     */
    fun updateActivePoint(userId: Long, activePoint: Int) {
        val request = UpdateActivePointInnerRequest(activePoint = activePoint)

        restClient.put()
            .uri("/internal/users/{userId}/active-point", userId)
            .body(request)
            .retrieve()
            .toBodilessEntity()
    }

    /**
     * 여러 유저 조회
     * GET /internal/users/batch?userIds=1&userIds=2...
     */
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
}