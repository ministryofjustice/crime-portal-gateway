package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.health

import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import uk.gov.justice.digital.hmpps.crimeportalgateway.application.TestMessagingConfig
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestMessagingConfig::class)
class InfoTest : IntegrationTestBase() {
    @LocalServerPort
    var port: Int = 0

    private fun getInfoJson(): JSONObject {
        val url = URL("http://localhost:$port/info")
        val conn = url.openConnection() as HttpURLConnection
        conn.requestMethod = "GET"
        conn.connect()
        val body = conn.inputStream.bufferedReader().readText()
        conn.disconnect()
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
