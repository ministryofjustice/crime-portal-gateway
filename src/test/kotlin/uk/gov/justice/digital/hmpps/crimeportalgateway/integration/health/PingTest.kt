package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.health

import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.crimeportalgateway.application.MessagingConfigTest
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase

@Import(MessagingConfigTest::class)
class PingTest : IntegrationTestBase() {

    @Test
    fun `Ping page reports pong`() {
        webTestClient.get()
            .uri("/ping")
            .exchange()
            .expectStatus()
            .isOk
    }

}
