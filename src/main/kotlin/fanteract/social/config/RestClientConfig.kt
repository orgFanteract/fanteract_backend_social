package fanteract.social.config

import org.springframework.cloud.client.loadbalancer.LoadBalanced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig {

    @Bean
    @LoadBalanced
    fun restClientBuilder(): RestClient.Builder {
        return RestClient.builder()
    }
}