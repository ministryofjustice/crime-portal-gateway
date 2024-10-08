package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.S3Service

@TestConfiguration
class TestMessagingConfig {
    @MockBean
    private lateinit var s3Service: S3Service
}
