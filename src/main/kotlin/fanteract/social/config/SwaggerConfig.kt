package fanteract.social.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.servers.Server
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@OpenAPIDefinition(
    servers = [
        Server(url = "http://localhost:8092", description = "로컬 서버"),
    ]
)
@Configuration
class SwaggerConfig {
    @Bean
    fun api(): OpenAPI {
        val apiKey =
            SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .`in`(SecurityScheme.In.HEADER)
                .name("Authorization")

        val securityRequirement =
            SecurityRequirement().addList("Bearer Token")

        return OpenAPI()
            .components(Components().addSecuritySchemes("Bearer Token", apiKey))
            .addSecurityItem(securityRequirement)
    }
}
