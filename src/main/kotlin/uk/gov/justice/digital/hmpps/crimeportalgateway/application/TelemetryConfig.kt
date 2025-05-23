package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import com.microsoft.applicationinsights.TelemetryClient
import com.microsoft.applicationinsights.telemetry.RequestTelemetry
import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext
import com.microsoft.applicationinsights.web.internal.ThreadContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.context.annotation.RequestScope
import java.util.Optional

@Profile(value = ["dev", "preprod", "prod"])
@Configuration
class TelemetryConfig {
    @Bean
    fun getTelemetryClient(): TelemetryClient = TelemetryClient()

    @Bean
    @RequestScope
    fun requestProperties(): Map<String, String> =
        Optional
            .ofNullable(ThreadContext.getRequestTelemetryContext())
            .map { obj: RequestTelemetryContext -> obj.httpRequestTelemetry }
            .map { obj: RequestTelemetry -> obj.properties }
            .orElse(emptyMap())

    @Bean
    fun getOperationId(): () -> String? =
        {
            ThreadContext
                .getRequestTelemetryContext()
                ?.httpRequestTelemetry
                ?.context
                ?.operation
                ?.id
        }
}
