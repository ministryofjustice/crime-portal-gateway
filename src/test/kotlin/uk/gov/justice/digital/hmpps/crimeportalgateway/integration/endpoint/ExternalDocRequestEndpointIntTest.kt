package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.endpoint

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectListing
import com.amazonaws.services.s3.model.S3ObjectSummary
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.util.Topics
import com.amazonaws.services.sqs.AmazonSQS
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
import uk.gov.justice.digital.hmpps.crimeportalgateway.xml.MessageDetail
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

    lateinit var queueUrl: String

    @BeforeEach
    fun beforeEach() {
        // this is all a bit horrible, using an old version of AWS SDK
        // would be best to bring in https://github.com/ministryofjustice/hmpps-spring-boot-sqs
        // using the spring-boot-2 branch as upgrading to Spring boot 3 brings in far reaching changes
        val topic = amazonSNS.createTopic(topicName)
        val queue = amazonSQS.createQueue("court-case-events-queue")
        queueUrl = queue.queueUrl
        val localstackUrl = localStackContainer?.getEndpointOverride(LocalStackContainer.Service.SNS).toString()

        Topics.subscribeQueue(
            amazonSNS,
            amazonSQS,
            topic.topicArn,
            queueUrl.replace("http://sqs.eu-west-2.localhost:4566", localstackUrl)
        )
        amazonS3.createBucket(bucketName, "eu-west-2")
    }

    @AfterEach
    fun afterEach() {
        // :-(
        // is the s3 upload even required? Nothinbg appears to read it
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
        // val message = amazonSQS.receiveMessage(ReceiveMessageRequest(queueUrl))

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
