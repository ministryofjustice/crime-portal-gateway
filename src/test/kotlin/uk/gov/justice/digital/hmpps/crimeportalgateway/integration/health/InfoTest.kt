package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.health

import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import uk.gov.justice.digital.hmpps.crimeportalgateway.application.TestMessagingConfig
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Import(TestMessagingConfig::class)
class InfoTest : IntegrationTestBase() {
    @Autowired
    lateinit var mockMvc: MockMvc

    private fun getInfoJson(): JSONObject {
        val mvcResult = mockMvc.get("/info") {
            accept(org.springframework.http.MediaType.APPLICATION_JSON)
        }.andReturn()
        val body = mvcResult.response.contentAsString
        return JSONObject(body)
    }

    @Test
    fun `Info page is accessible`() {
        val json = getInfoJson()
        assertThat(json.getJSONObject("build").getString("name")).isEqualTo("crime-portal-gateway")
    }

    @Test
    fun `Info page reports version`() {
        val json = getInfoJson()
        val version = json.getJSONObject("build").getString("version")
        assertThat(version).startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
    }
}
