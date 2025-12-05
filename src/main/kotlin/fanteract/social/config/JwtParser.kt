package fanteract.social.config

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value

class JwtParser {
    companion object {
        fun extractKey(request: HttpServletRequest, key: String): Long{
            val userId = request.getAttribute(key) as String

            return userId.toLong()
        }

        fun extractToken(
            token: String,
            @Value($$"${jwt.secret}") jwtSecret: String,
        ): Long {
            if (!token.startsWith("Bearer "))
                throw NoSuchElementException("조건에 맞는 토큰이 존재하지 않습니다")

            val token = token.substringAfter("Bearer ")
            val secretKey = Keys.hmacShaKeyFor(jwtSecret.toByteArray())

            val userId =
                Jwts.parser().verifyWith(secretKey).build()
                    .parseSignedClaims(token).payload.subject.toLong()

            return userId
        }
    }
}
