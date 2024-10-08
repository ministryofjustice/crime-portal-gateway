package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.endpoint

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
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
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryEventType.COURT_LIST_MESSAGE_RECEIVED
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.countMessagesOnQueue
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.xml.transform.Source

class ExternalDocRequestEndpointIntTest : IntegrationTestBase() {
    @Autowired
    private lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var amazonS3: AmazonS3

    @Autowired
    lateinit var hmppsQueueService: HmppsQueueService
    val courtCaseEventsQueue by lazy {
        hmppsQueueService.findByQueueId("courtcaseeventsqueue")
    }

    @Value("\${aws.s3.bucket_name}")
    lateinit var bucketName: String

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun beforeEach() {
        courtCaseEventsQueue?.sqsClient?.purgeQueue(PurgeQueueRequest.builder().queueUrl(courtCaseEventsQueue!!.queueUrl).build())
        amazonS3.createBucket(bucketName, "eu-west-2")
    }

    @AfterEach
    fun afterEach() {
        // :-( deleting everything in the S3 bucket
        // is the s3 upload even required? Nothing appears to read it
        var objectListing: ObjectListing = amazonS3.listObjects(bucketName)
        while (true) {
            val objIter: Iterator<S3ObjectSummary> = objectListing.objectSummaries.iterator()
            while (objIter.hasNext()) {
                amazonS3.deleteObject(bucketName, objIter.next().key)
            }

            if (objectListing.isTruncated) {
                objectListing = amazonS3.listNextBatchOfObjects(objectListing)
            } else {
                break
            }
        }
        amazonS3.deleteBucket(bucketName)
    }

    @Test
    fun `should send SQS message for each case`() {
        val externalDoc1 = readFile("src/test/resources/soap/sample-request.xml")
        val requestEnvelope: Source = StringSource(externalDoc1)
        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageComment", namespaces)
                    .evaluatesTo("MessageComment"),
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageStatus", namespaces)
                    .evaluatesTo("Success"),
            )
            .andExpect(xpath("//ns3:Acknowledgement/Ack/TimeStamp", namespaces).exists())
            .andExpect(noFault())

        verify(telemetryService).trackEvent(
            COURT_LIST_MESSAGE_RECEIVED,
            mapOf(
                "courtCode" to "B10JQ",
                "courtRoom" to "0",
                "hearingDate" to "2020-10-26",
                "fileName" to "5_26102020_2992_B10JQ00_ADULT_COURT_LIST_DAILY",
            ),
        )

        val firstCase = CaseDetails(caseNo = 166662981, defendantName = "MR Abraham LINCOLN", pnc = "20030011985X", cro = "CR0006100061")
        val secondCase = CaseDetails(caseNo = 1777732980, defendantName = "Mr Theremin MELLOTRON", pnc = "20120052494Q", cro = "CR0006200062")
        checkMessage(listOf(firstCase, secondCase))

        checkS3Upload("2020-10-26-B10")
    }

    @Test
    fun `should not enqueue message when court is not processed but return acknowledgement`() {
        val externalDoc1 = readFile("src/test/resources/soap/ignored-courts.xml")
        val requestEnvelope: Source = StringSource(externalDoc1)

        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageComment", namespaces)
                    .evaluatesTo("MessageComment"),
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageStatus", namespaces)
                    .evaluatesTo("Success"),
            )
            .andExpect(xpath("//ns3:Acknowledgement/Ack/TimeStamp", namespaces).exists())
            .andExpect(noFault())

        checkMessagesOnQueue(0)
        checkS3Upload("2020-10-26-B10")
    }

    @Test
    fun `given no court present`() {
        val requestEnvelope: Source = StringSource(readFile("src/test/resources/soap/sample-request-invalid-xml.xml"))

        mockClient.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
            .andExpect(validPayload(xsdResource))
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageComment", namespaces)
                    .evaluatesTo("MessageComment"),
            )
            .andExpect(
                xpath("//ns3:Acknowledgement/Ack/MessageStatus", namespaces)
                    .evaluatesTo("Success"),
            )
            .andExpect(xpath("//ns3:Acknowledgement/Ack/TimeStamp", namespaces).exists())
            .andExpect(noFault())

        checkMessagesOnQueue(0)
        val date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)

        checkS3Upload("fail-$date")
    }

    private fun checkMessagesOnQueue(count: Int) {
        val numberOfMessagesOnQueue = courtCaseEventsQueue?.sqsClient?.countMessagesOnQueue(courtCaseEventsQueue?.queueUrl!!)?.get()!!
        assertThat(numberOfMessagesOnQueue).isEqualTo(count)
    }

    private fun countMessagesOnQueue() = courtCaseEventsQueue?.sqsClient?.countMessagesOnQueue(courtCaseEventsQueue?.queueUrl!!)!!.get()

    private fun checkS3Upload(fileNameStart: String) {
        val items = amazonS3.listObjects(bucketName).objectSummaries
        assertThat(items.size).isEqualTo(1)
        assertThat(items[0].key.startsWith(fileNameStart)).isTrue()
        val s3Object = amazonS3.getObject(bucketName, items[0].key)
        val startOfDoc = s3Object.objectContent.readNBytes(1000).toString(Charsets.UTF_8)
        println(startOfDoc)
        assertThat(
            @Suppress("ktlint:standard:max-line-length")
            startOfDoc.startsWith(
                "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:ExternalDocumentRequest xmlns:ns2=\"http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest\"",
            ),
        ).isTrue()
    }

    private fun readFile(fileName: String): String = File(fileName).readText(Charsets.UTF_8)

    private fun checkMessage(expectedCases: List<CaseDetails>) {
        var cases = mutableListOf<CaseDetails>()
        while (countMessagesOnQueue() > 0) {
            val message = courtCaseEventsQueue?.sqsClient?.receiveMessage(ReceiveMessageRequest.builder().queueUrl(courtCaseEventsQueue?.queueUrl!!).build())!!.get()
            val sqsMessage: SQSMessage = objectMapper.readValue(message.messages()[0].body(), SQSMessage::class.java)

            cases.add(objectMapper.readValue(sqsMessage.message, CaseDetails::class.java))
        }
        assertThat(cases).containsAll(expectedCases)
    }

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

@JsonIgnoreProperties(ignoreUnknown = true)
data class SQSMessage(
    @JsonProperty("Message")
    val message: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CaseDetails(
    val caseNo: Int,
    val defendantName: String,
    val cro: String,
    val pnc: String,
)
