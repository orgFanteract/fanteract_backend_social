package fanteract.social.config

import fanteract.social.exception.ExceptionType
import fanteract.social.exception.MessageType
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value

class JwtParser {
    companion object {
        fun extractKey(
            request: HttpServletRequest,
            key: String,
        ): Long {
            val userId = request.getAttribute(key) as String

            return userId.toLong()
        }

        fun extractToken(
            token: String,
            @Value($$"${jwt.secret}") jwtSecret: String,
        ): Long {
            if (!token.startsWith("Bearer ")) {
                throw ExceptionType.withType(MessageType.INVALID_TOKEN)
            }

            val token = token.substringAfter("Bearer ")
            val secretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

            val userId =
                Jwts
                    .parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .payload.subject
                    .toLong()

            return userId
        }
    }
}
