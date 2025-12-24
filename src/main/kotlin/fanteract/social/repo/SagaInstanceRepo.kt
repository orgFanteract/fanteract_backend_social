package fanteract.social.repo

import fanteract.social.entity.SagaInstance
import org.springframework.data.jpa.repository.JpaRepository

interface SagaInstanceRepo : JpaRepository<SagaInstance, String>