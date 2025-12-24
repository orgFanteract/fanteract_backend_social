package fanteract.social.exception

import org.springframework.http.HttpStatus

enum class MessageType(
    private val code: String,
    private val message: String,
    private val status: HttpStatus
) {
    NOT_EXIST("NOT_EXIST", "조건에 맞는 대상이 존재하지 않습니다", HttpStatus.NOT_FOUND),
    ALREADY_EXIST("ALREADY_EXIST", "조건에 맞는 대상이 이미 존재합니다", HttpStatus.NOT_FOUND),
    INVALID_TOKEN("INVALID_TOKEN", "조건에 맞는 토큰이 존재하지 않습니다", HttpStatus.BAD_REQUEST),
    NOT_ENOUGH_BALANCE("NOT_ENOUGH_BALANCE", "비용이 부족합니다", HttpStatus.BAD_REQUEST),
    INVALID_ACTION("INVALID_ACTION", "잘못된 행동입니다", HttpStatus.BAD_REQUEST),
    INVALID_CONNECTED_SERVICE("INVALID_CONNECTED_SERVICE", "연결된 서비스에 오류가 있습니다", HttpStatus.BAD_REQUEST),
    BASIC_EXCEPTION("BASIC_EXCEPTION", "테스트를 위한 강제 예외 처리", HttpStatus.BAD_REQUEST),
    ;

    fun getCode(): String = this.code
    fun getMessage(): String = this.message
    fun getStatus(): HttpStatus = this.status
}
