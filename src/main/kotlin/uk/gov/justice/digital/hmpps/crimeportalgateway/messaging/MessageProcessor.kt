package uk.gov.justice.digital.hmpps.crimeportalgateway.messaging

import com.fasterxml.jackson.core.JsonProcessingException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest.Block
import uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest.Document
import uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest.ExternalDocumentRequest
import uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest.Info
import uk.gov.justice.digital.hmpps.crimeportalgateway.model.externaldocumentrequest.Session
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService

@Service
class MessageProcessor(
    @Autowired
    private val messageParser: MessageParser<ExternalDocumentRequest>,
    @Autowired
    private val messageNotifier: MessageNotifier,
    @Autowired
    private val telemetryService: TelemetryService
) {

    @Throws(JsonProcessingException::class)
    fun process(message: String, messageId: String) {
        val externalDocumentRequest = messageParser.parseMessage(message, ExternalDocumentRequest::class.java)

        val documents = externalDocumentRequest.documentWrapper.document
        trackCourtListReceipt(documents, messageId)

        return documents
            .stream()
            .flatMap { document: Document ->
                document.data.job.sessions.stream()
            }
            .flatMap { session: Session ->
                session.blocks.stream()
            }
            .flatMap { block: Block ->
                block.cases.stream()
            }
            .forEach {
                log.debug("Sending {}", it.caseNo)
                messageNotifier.send(it, "some-id")
            }
    }

    private fun trackCourtListReceipt(documents: List<Document>, messageId: String) {
        documents.stream()
            .map { it.info }
            .distinct()
            .forEach { info: Info ->
                run {
                    log.debug("Track court list event $info")
                    telemetryService.trackCourtListEvent(info, messageId)
                }
            }
    }

    companion object {
        private val log = LoggerFactory.getLogger(this::class.java)
    }
}
