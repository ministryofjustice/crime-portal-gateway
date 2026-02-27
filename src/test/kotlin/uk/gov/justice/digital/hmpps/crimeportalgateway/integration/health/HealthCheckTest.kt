package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.crimeportalgateway.application.TestMessagingConfig
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestMessagingConfig::class)
class HealthCheckTest : IntegrationTestBase() {
    @LocalServerPort
    var port: Int = 0

    private fun getResponse(path: String): Pair<Int, String> {
        val url = URL("http://localhost:$port$path")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connect()
        val status = conn.responseCode
        val body = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
        return status to body
    }

    @Test
    fun `Health page reports ok`() {
        val (status, body) = getResponse("/health")
        assertThat(status).isEqualTo(HttpStatus.OK.value())
        assertThat(body).contains("\"status\":\"UP\"")
    }

    @Test
    fun `Health info reports version`() {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)
        val (status, body) = getResponse("/health")
        assertThat(status).isEqualTo(HttpStatus.OK.value())
        assertThat(body).contains(now)
    }

    @Test
    fun `Health ping page is accessible`() {
        val (status, body) = getResponse("/health/ping")
        assertThat(status).isEqualTo(HttpStatus.OK.value())
        assertThat(body).contains("\"status\":\"UP\"")
    }

    @Test
    fun `readiness reports ok`() {
        val (status, body) = getResponse("/health/readiness")
        assertThat(status).isEqualTo(HttpStatus.OK.value())
        assertThat(body).contains("\"status\":\"UP\"")
    }

    @Test
    fun `liveness reports ok`() {
        val (status, body) = getResponse("/health/liveness")
        assertThat(status).isEqualTo(HttpStatus.OK.value())
        assertThat(body).contains("\"status\":\"UP\"")
    }
}
