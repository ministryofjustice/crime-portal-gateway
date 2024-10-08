package uk.gov.justice.digital.hmpps.crimeportalgateway.integration

import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.context.ApplicationContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.ws.test.server.MockWebServiceClient
import uk.gov.justice.digital.hmpps.crimeportalgateway.service.TelemetryService

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
abstract class IntegrationTestBase {
    @Autowired
    lateinit var webTestClient: WebTestClient

    @SpyBean
    private lateinit var telemetryService: TelemetryService

    @Autowired
    private lateinit var applicationContext: ApplicationContext

    lateinit var mockClient: MockWebServiceClient

    @BeforeEach
    fun before() {
        mockClient = MockWebServiceClient.createClient(applicationContext)
    }
}
