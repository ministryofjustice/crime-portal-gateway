package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.endpoint

import com.amazonaws.services.sns.AmazonSNS
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.ws.test.server.RequestCreators
import org.springframework.ws.test.server.ResponseMatchers.noFault
import org.springframework.ws.test.server.ResponseMatchers.validPayload
import org.springframework.ws.test.server.ResponseMatchers.xpath
import org.springframework.xml.transform.StringSource
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryEventType
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService
import uk.gov.justice.digital.hmpps.crimeportalgateway.xml.MessageDetail
import java.io.File
import javax.xml.transform.Source

class ExternalDocRequestEndpointIntTest : IntegrationTestBase() {

    @Autowired
    private lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var amazonSNS: AmazonSNS

    @Value("\${aws.sns.court-case-events-topic-name}")
    lateinit var topicName: String

    @BeforeEach
    fun beforeEach() {
        amazonSNS.createTopic(topicName)
    }

    @Test
    fun `should enqueue message and return successful acknowledgement`() {
        val externalDoc1 = readFile("src/test/resources/soap/sample-request.xml")
        val requestEnvelope: Source = StringSource(externalDoc1)
        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageComment", namespaces)
                    .evaluatesTo("MessageComment")
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageStatus", namespaces)
                    .evaluatesTo("Success")
            )
            .andExpect(xpath("//ns3:Acknowledgement/Ack/TimeStamp", namespaces).exists())
            .andExpect(noFault())

        verify(telemetryService).trackEvent(
            TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED,
            mapOf(

                "courtCode" to "B10JQ",
                "courtRoom" to "0",
                "hearingDate" to "2020-10-26",
                "fileName" to "5_26102020_2992_B10JQ00_ADULT_COURT_LIST_DAILY"
            )
        )

        val expectedMessageDetail = MessageDetail(courtCode = "B10JQ", courtRoom = 0, hearingDate = "2020-10-26")

        // possibly check S3 upload
        // definitely get sqs message
    }

    @Test
    fun `should not enqueue message when court is not processed but return acknowledgement`() {
        val externalDoc1 = readFile("src/test/resources/soap/ignored-courts.xml")
        val requestEnvelope: Source = StringSource(externalDoc1)

        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageComment", namespaces)
                    .evaluatesTo("MessageComment")
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageStatus", namespaces)
                    .evaluatesTo("Success")
            )
            .andExpect(xpath("//ns3:Acknowledgement/Ack/TimeStamp", namespaces).exists())
            .andExpect(noFault())

        // possibly check S3 upload
        // check no message on queue
    }

    @Test
    fun `given no court present`() {
        val requestEnvelope: Source = StringSource(readFile("src/test/resources/soap/sample-request-invalid-xml.xml"))

        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageComment", namespaces)
                    .evaluatesTo("MessageComment")
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageStatus", namespaces)
                    .evaluatesTo("Success")
            )
            .andExpect(xpath("//ns3:Acknowledgement/Ack/TimeStamp", namespaces).exists())
            .andExpect(noFault())

        // possibly check S3 upload
        // check no message on queue
    }

    fun readFile(fileName: String): String = File(fileName).readText(Charsets.UTF_8)

    companion object {

        private lateinit var xsdResource: Resource

        private val namespaces = HashMap<String, String>()

        @JvmStatic
        @BeforeAll
        fun beforeAll() {
            // namespaces["ns35"] = "http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest"
            namespaces["ns3"] = "http://www.justice.gov.uk/magistrates/ack"
            namespaces["env"] = "http://www.w3.org/2003/05/soap-envelope"
            val resourceLoader: ResourceLoader = DefaultResourceLoader()
            xsdResource = resourceLoader.getResource("xsd/generic/Acknowledgement/Acknowledgement.xsd")
        }
    }
}
