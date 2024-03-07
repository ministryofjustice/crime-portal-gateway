package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import com.amazonaws.services.sns.AmazonSNS
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.S3Service
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService

@TestConfiguration
class MessagingConfigTest {

    @MockBean
    private lateinit var amazonSNS: AmazonSNS

    @MockBean
    private lateinit var telemetryService: TelemetryService

    @MockBean
    private lateinit var s3Service: S3Service
}
