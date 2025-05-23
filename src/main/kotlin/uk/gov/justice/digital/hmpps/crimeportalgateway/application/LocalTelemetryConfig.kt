package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.UUID

@Profile(value = ["local", "test"])
@Configuration
class LocalTelemetryConfig {
    @Bean
    fun getTelemetryClient(): TelemetryClient = TelemetryClient()

    @Bean
    fun getOperationId(): () -> String? = { UUID.randomUUID().toString() }
}
