package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.crimeportalgateway.application.TestMessagingConfig
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.function.Consumer

@Import(TestMessagingConfig::class)
class HealthCheckTest : IntegrationTestBase() {
    @Test
    fun `Health page reports ok`() {
        webTestClient
            .get()
            .uri("/health")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("status")
            .isEqualTo("UP")
    }

    @Test
    fun `Health info reports version`() {
        webTestClient
            .get()
            .uri("/health")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("components.healthInfo.details.version")
            .value(
                Consumer<String> {
                    assertThat(it).startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
                },
            )
    }

    @Test
    fun `Health ping page is accessible`() {
        webTestClient
            .get()
            .uri("/health/ping")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("status")
            .isEqualTo("UP")
    }

    @Test
    fun `readiness reports ok`() {
        webTestClient
            .get()
            .uri("/health/readiness")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("status")
            .isEqualTo("UP")
    }

    @Test
    fun `liveness reports ok`() {
        webTestClient
            .get()
            .uri("/health/liveness")
            .exchange()
            .expectStatus()
            .isOk
            .expectBody()
            .jsonPath("status")
            .isEqualTo("UP")
    }
}
