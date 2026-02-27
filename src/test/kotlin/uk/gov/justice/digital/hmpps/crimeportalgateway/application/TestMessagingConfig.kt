package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.S3Service

@TestConfiguration
class TestMessagingConfig {
    @MockitoBean
    private lateinit var s3Service: S3Service
}
