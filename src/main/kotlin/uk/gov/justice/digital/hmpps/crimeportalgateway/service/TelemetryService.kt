package uk.gov.justice.digital.hmpps.crimeportalgateway.service

import com.microsoft.applicationinsights.TelemetryClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest.Info

@Component
class TelemetryService(@Autowired private val telemetryClient: TelemetryClient) {

    fun trackEvent(eventType: TelemetryEventType) {
        telemetryClient.trackEvent(eventType.eventName)
    }

    fun trackEvent(eventType: TelemetryEventType, customDimensions: Map<String, String?>) {
        telemetryClient.trackEvent(eventType.eventName, customDimensions, null)
    }

    fun trackCourtListEvent(info: Info, messageId: String) {
        val properties = mapOf(COURT_CODE_KEY to info.ouCode, HEARING_DATE_KEY to info.dateOfHearing.toString(), SQS_MESSAGE_ID_KEY to messageId)

        telemetryClient.trackEvent(TelemetryEventType.COURT_LIST_RECEIVED.eventName, properties, emptyMap())
    }

    companion object {
        const val COURT_CODE_KEY = "courtCode"

        const val HEARING_DATE_KEY = "hearingDate"
        const val SQS_MESSAGE_ID_KEY = "sqsMessageId"
    }
}
