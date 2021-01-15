package uk.gov.justice.digital.hmpps.crimeportalgateway.endpoint

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.ws.server.endpoint.annotation.Endpoint
import org.springframework.ws.server.endpoint.annotation.PayloadRoot
import org.springframework.ws.server.endpoint.annotation.RequestPayload
import org.springframework.ws.server.endpoint.annotation.ResponsePayload
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.SqsService
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryEventType
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService
import uk.gov.justice.digital.hmpps.crimeportalgateway.xml.DocumentUtils
import uk.gov.justice.magistrates.external.externaldocumentrequest.AckType
import uk.gov.justice.magistrates.external.externaldocumentrequest.Acknowledgement
import uk.gov.justice.magistrates.external.externaldocumentrequest.ExternalDocumentRequest
import java.io.StringWriter
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import javax.xml.bind.JAXBContext
import javax.xml.bind.Marshaller
import javax.xml.validation.Schema

@Endpoint
class ExternalDocRequestEndpoint(
    @Value("#{'\${included-court-codes}'.split(',')}") private val includedCourts: Set<String>,
    @Value("\${enqueue-msg-async:true}") private val enqueueMsgAsync: Boolean,
    @Value("\${use-xpath-for-court-code:true}") private val xPathForCourtCode: Boolean,
    @Autowired private val telemetryService: TelemetryService,
    @Autowired private val sqsService: SqsService,
    @Autowired private val jaxbContext: JAXBContext,
    @Autowired private val validationSchema: Schema?
) {

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = REQUEST_LOCAL_NAME)
    @ResponsePayload
    fun processRequest(@RequestPayload request: ExternalDocumentRequest): Acknowledgement {
        log.info("Request payload received. {}", request.documents?.toString())

        when (enqueueMsgAsync) {
            true -> {
                CompletableFuture
                    .supplyAsync<Any> { enqueueMessage(request) }
                    .exceptionally { exception: Throwable? ->
                        log.error("Error from enqueuing message", exception)
                    }
            }
            false -> {
                enqueueMessage(request)
            }
        }

        return Acknowledgement().apply {
            ackType = AckType().apply {
                messageComment = "MessageComment"
                messageStatus = SUCCESS_MESSAGE_STATUS
                timeStamp = LocalDateTime.now()
            }
        }
    }

    fun enqueueMessage(request: ExternalDocumentRequest) {
        val courtCode = request.documents?.any?.let { DocumentUtils.getCourtCode(it, xPathForCourtCode) }
        when (includedCourts.contains(courtCode)) {
            true -> {
                val messageId = sqsService.enqueueMessage(marshal(request))
                telemetryService.trackEvent(TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED)
                log.info(String.format(SUCCESS_MESSAGE_COMMENT, courtCode, messageId))
            }
            false -> {
                log.info(courtCode?.let { String.format(IGNORED_MESSAGE_UNKNOWN_COURT, it) } ?: IGNORED_MESSAGE_NO_COURT)
            }
        }
    }

    private fun marshal(request: ExternalDocumentRequest): String {
        val marshaller: Marshaller = jaxbContext.createMarshaller()
        validationSchema?.let { marshaller.schema = it }
        val sw = StringWriter()
        marshaller.marshal(request, sw)
        return sw.toString()
    }

    companion object {
        const val SUCCESS_MESSAGE_STATUS = "Success"
        const val SUCCESS_MESSAGE_COMMENT = "Message successfully enqueued for court %s with id %s"
        const val IGNORED_MESSAGE_UNKNOWN_COURT = "Message ignored - the court %s in the message is not processed"
        const val IGNORED_MESSAGE_NO_COURT = "Message ignored - no court code found in the message"
        const val NAMESPACE_URI = "http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest"
        const val REQUEST_LOCAL_NAME = "ExternalDocumentRequest"
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
