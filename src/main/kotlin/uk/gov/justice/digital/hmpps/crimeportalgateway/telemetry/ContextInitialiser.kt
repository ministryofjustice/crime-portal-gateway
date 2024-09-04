package uk.gov.justice.digital.hmpps.crimeportalgateway.telemetry

import com.microsoft.applicationinsights.extensibility.context.ComponentContext
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.ConcurrentHashMap

@Configuration
class ContextInitialiser(
    buildProperties: BuildProperties
) {
    val version: String = buildProperties.version

    @Bean
    fun versionContextInitializer(): String {
        ComponentContext(ConcurrentHashMap()).setVersion(version)
        return version
    }
}
