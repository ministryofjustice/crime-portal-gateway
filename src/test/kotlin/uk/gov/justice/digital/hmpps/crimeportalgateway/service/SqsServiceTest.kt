package uk.gov.justice.digital.hmpps.crimeportalgateway.service

import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.model.GetQueueUrlResult
import com.amazonaws.services.sqs.model.SendMessageRequest
import com.amazonaws.services.sqs.model.SendMessageResult
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class SqsServiceTest {

    @Mock
    private lateinit var amazonSQS: AmazonSQS

    private lateinit var sqsService: SqsService

    @BeforeEach
    fun beforeEach() {
        sqsService = SqsService(queueName, amazonSQS)
    }

    @Test
    fun `should give queue as available`() {
        whenever(amazonSQS.getQueueUrl(queueName)).thenReturn(GetQueueUrlResult().withQueueUrl(queueUrl))

        assertThat(sqsService.isQueueAvailable()).isTrue
    }

    @Test
    fun `should not give queue as available when not`() {
        whenever(amazonSQS.getQueueUrl(queueName)).thenReturn(GetQueueUrlResult())

        assertThat(sqsService.isQueueAvailable()).isFalse
    }

    @Test
    fun `should enqueue a message`() {
        whenever(amazonSQS.getQueueUrl(queueName)).thenReturn(GetQueueUrlResult().withQueueUrl(queueUrl))

        val msgRequest = SendMessageRequest()
            .withQueueUrl(queueUrl)
            .withMessageBody("Hello World")

        whenever(amazonSQS.sendMessage(msgRequest)).thenReturn(SendMessageResult().withMessageId("ID"))

        val id = sqsService.enqueueMessage("Hello World")

        assertThat(id).isEqualTo("ID")
    }

    companion object {
        private const val queueName = "crime-portal-gateway-queue"
        private const val queueUrl = "http://localhost:4566/000000000000/crime-portal-gateway-queue"
    }
}
