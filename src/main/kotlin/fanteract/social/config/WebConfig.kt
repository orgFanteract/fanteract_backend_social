package fanteract.social.config

import fanteract.social.interceptor.AuthInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val authInterceptor: AuthInterceptor
): WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(authInterceptor)
    }

//    override fun addCorsMappings(registry: CorsRegistry) {
//        registry.addMapping("/**")
//            .allowedOrigins("*")                // 모든 도메인 허용
//            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
//            .allowedHeaders("*")
//            .allowCredentials(false)            // JWT라면 false 추천 (true는 특정 조건 필요)
//            .maxAge(3600)                       // preflight 캐시 시간
//    }
}