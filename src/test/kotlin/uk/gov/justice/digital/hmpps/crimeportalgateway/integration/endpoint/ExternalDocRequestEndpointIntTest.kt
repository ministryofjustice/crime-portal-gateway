package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.endpoint

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.util.Topics
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.model.ReceiveMessageRequest
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
import org.testcontainers.containers.localstack.LocalStackContainer
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryEventType
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService
import java.io.File
import javax.xml.transform.Source

class ExternalDocRequestEndpointIntTest : IntegrationTestBase() {

    @Autowired
    private lateinit var telemetryService: TelemetryService

    @Autowired
    lateinit var amazonSNS: AmazonSNS

    @Autowired
    lateinit var amazonSQS: AmazonSQS

    @Autowired
    lateinit var amazonS3: AmazonS3

    @Value("\${aws.sns.court-case-events-topic-name}")
    lateinit var topicName: String

    @Value("\${aws.s3.bucket_name}")
    lateinit var bucketName: String

    @Autowired
    lateinit var objectMapper: ObjectMapper

    lateinit var queueUrl: String

    @BeforeEach
    fun beforeEach() {
        // this is all a bit horrible, using an old version of AWS SDK
        // would be best to bring in https://github.com/ministryofjustice/hmpps-spring-boot-sqs
        // using the spring-boot-2 branch as upgrading to Spring boot 3 brings in far reaching changes
        val topic = amazonSNS.createTopic(topicName)
        val queue = amazonSQS.createQueue("court-case-events-queue")
        val localstackUrl = localStackContainer?.getEndpointOverride(LocalStackContainer.Service.SNS).toString()
        queueUrl = queue.queueUrl.replace("http://sqs.eu-west-2.localhost:4566", localstackUrl)

        Topics.subscribeQueue(
            amazonSNS,
            amazonSQS,
            topic.topicArn,
            queueUrl
        )
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

        checkMessage(amazonSQS.receiveMessage(ReceiveMessageRequest(queueUrl)).messages[0], 166662981)
        checkMessage(amazonSQS.receiveMessage(ReceiveMessageRequest(queueUrl)).messages[0], 1777732980)
        // possibly check S3 upload
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
        val numberOfMessagesOnQueue = countMessagesOnQueue()
        assertThat(numberOfMessagesOnQueue).isEqualTo("0")
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

        val numberOfMessagesOnQueue = countMessagesOnQueue()
        assertThat(numberOfMessagesOnQueue).isEqualTo("0")
        // possibly check S3 upload
    }

    private fun countMessagesOnQueue() = amazonSQS.getQueueAttributes(queueUrl, listOf("ApproximateNumberOfMessages")).attributes["ApproximateNumberOfMessages"]

    private fun readFile(fileName: String): String = File(fileName).readText(Charsets.UTF_8)

    private fun checkMessage(message: Message, caseNo: Int) {
        val messageBody = objectMapper.readValue(message.body, SQSMessage::class.java)
        val case = objectMapper.readValue(messageBody.message, CaseDetails::class.java)
        assertThat(case.caseNo).isEqualTo(caseNo)
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
    val message: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class CaseDetails(

    val caseNo: Int
)
