package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.crimeportalgateway.application.TestMessagingConfig
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(TestMessagingConfig::class)
@AutoConfigureMockMvc
class HealthCheckTest : IntegrationTestBase() {
    @Autowired
    lateinit var mockMvc: MockMvc

    private fun getResponse(path: String): Pair<Int, String> {
        val mvcResult = mockMvc.get(path) {
            accept(MediaType.APPLICATION_JSON)
        }.andReturn()
        val status = mvcResult.response.status
        val body = mvcResult.response.contentAsString
        return status to body
    }

    @Test
    fun `Health page reports ok`() {
        val (status, body) = getResponse("/health")
        assertThat(status).isEqualTo(200)
        assertThat(body).contains("\"status\":\"UP\"")
    }

    @Test
    fun `Health info reports version`() {
        val now = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE)
        val (status, body) = getResponse("/health")
        assertThat(status).isEqualTo(200)
        assertThat(body).contains(now)
    }

    @Test
    fun `Health ping page is accessible`() {
        val (status, body) = getResponse("/health/ping")
        assertThat(status).isEqualTo(200)
        assertThat(body).contains("\"status\":\"UP\"")
    }

    @Test
    fun `readiness reports ok`() {
        val (status, body) = getResponse("/health/readiness")
        assertThat(status).isEqualTo(200)
        assertThat(body).contains("\"status\":\"UP\"")
    }

    @Test
    fun `liveness reports ok`() {
        val (status, body) = getResponse("/health/liveness")
        assertThat(status).isEqualTo(200)
        assertThat(body).contains("\"status\":\"UP\"")
    }
}
