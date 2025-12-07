package fanteract.social.exception

import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.servlet.NoHandlerFoundException

@ControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /** 비즈니스 예외 (ExceptionType) */
    @ExceptionHandler(ExceptionType::class)
    fun handleBusinessException(
        e: ExceptionType,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = e.type.getCode(),
            message = e.message,
            //path = request.requestURI
        )
        return ResponseEntity.status(e.type.getStatus()).body(body)
    }

    /** 지원하지 않는 HTTP 메서드 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(
        e: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = "METHOD_NOT_ALLOWED",
            message = "허용되지 않은 HTTP 메서드입니다.",
            //path = request.requestURI
        )
        return ResponseEntity.status(405).body(body)
    }

    /** 요청 경로 없음 (404) */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNotFound(
        e: NoHandlerFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = "NOT_FOUND",
            message = "요청한 경로가 존재하지 않습니다.",
            //path = request.requestURI
        )
        return ResponseEntity.status(404).body(body)
    }

    /** 파라미터 타입 불일치 등 */
    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleTypeMismatch(
        e: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = "INVALID_PARAMETER",
            message = "잘못된 파라미터 값입니다. ${e.name}=${e.value}",
            //path = request.requestURI
        )
        return ResponseEntity.status(400).body(body)
    }

    /** 그 외 모든 예외 */
    @ExceptionHandler(Exception::class)
    fun handleAllException(
        e: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val body = ErrorResponse(
            code = "INTERNAL_SERVER_ERROR",
            message = e.message ?: "서버 내부 오류가 발생했습니다.",
            //path = request.requestURI
        )
        return ResponseEntity.status(500).body(body)
    }
}
