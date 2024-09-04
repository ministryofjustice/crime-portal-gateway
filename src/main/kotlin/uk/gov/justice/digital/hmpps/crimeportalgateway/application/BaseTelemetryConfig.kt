package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata
import org.springframework.lang.NonNull
import org.springframework.util.StringUtils

open class BaseTelemetryConfig {
    class AppInsightKeyAbsentCondition : Condition {
        override fun matches(
            @NonNull context: ConditionContext,
            @NonNull metadata: AnnotatedTypeMetadata,
        ): Boolean {
            return !StringUtils.hasLength(context.environment.getProperty("application.insights.ikey"))
        }
    }
}
