package fanteract.social

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jpa.repository.config.EnableJpaAuditing

@EnableJpaAuditing
@SpringBootApplication
class SocialApplication

fun main(args: Array<String>) {
	runApplication<SocialApplication>(*args)
}
