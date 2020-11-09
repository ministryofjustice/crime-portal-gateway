package uk.gov.justice.digital.hmpps.crimeportalgateway.integration.endpoint

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Import
import org.springframework.core.io.DefaultResourceLoader
import org.springframework.core.io.Resource
import org.springframework.core.io.ResourceLoader
import org.springframework.test.context.ActiveProfiles
import org.springframework.ws.test.server.MockWebServiceClient
import org.springframework.ws.test.server.RequestCreators
import org.springframework.ws.test.server.ResponseMatchers
import org.springframework.ws.test.server.ResponseMatchers.noFault
import org.springframework.ws.test.server.ResponseMatchers.validPayload
import org.springframework.xml.transform.StringSource
import uk.gov.justice.digital.hmpps.crimeportalgateway.config.WebServiceConfig
import uk.gov.justice.digital.hmpps.crimeportalgateway.config.WebServiceConfigTest
import uk.gov.justice.digital.hmpps.crimeportalgateway.integration.IntegrationTestBase
import javax.xml.transform.Source

class ExternalDocRequestEndpointIntTest : IntegrationTestBase() {

    private val resourceLoader: ResourceLoader = DefaultResourceLoader()

    @Autowired
    private val applicationContext: ApplicationContext? = null

    private var mockClient: MockWebServiceClient? = null

    @BeforeEach
    fun before() {
        mockClient = MockWebServiceClient.createClient(applicationContext)
    }

    @Test
    fun `should return body acknowledgement`() {
        val requestEnvelope: Source = StringSource(
                "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\" \n"
                        + "\t\t\txmlns:ns35=\"http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest\">\n"
                        + "   <soap:Header xmlns:wsa=\"http://www.w3.org/2005/08/addressing\">\n"
                        + "      <wsa:Action>externalDocument</wsa:Action>\n"
                        + "      <wsa:From>\n"
                        + "         <wsa:Address>CP_NPS_ML</wsa:Address>\n"
                        + "      </wsa:From>\n"
                        + "      <wsa:MessageID>09233523-345b-4351-b623-5dsf35sgs5d6</wsa:MessageID>\n"
                        + "      <wsa:RelatesTo>RelatesToValue</wsa:RelatesTo>\n"
                        + "      <wsa:To>CP_NPS</wsa:To>\n"
                        + "   </soap:Header>\n"
                        + "   <soap:Body>\n"
                        + "      <ns35:ExternalDocumentRequest><documents></documents>"
                        + "      </ns35:ExternalDocumentRequest>\n"
                        + "   </soap:Body>\n"
                        + "</soap:Envelope>")

        val ns = HashMap<String, String>();
        ns.put("ns3", "http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest")

        mockClient!!.sendRequest(RequestCreators.withSoapEnvelope(requestEnvelope))
//                .andExpect(validPayload(resource("xsd/cp/external/ExternalDocumentRequest.xsd")))
                .andExpect(ResponseMatchers.xpath("//ns3:Acknowledgement/ackType/MessageComment", ns).exists())
                .andExpect(ResponseMatchers.xpath("//ns3:Acknowledgement/ackType/MessageStatus", ns).exists())
                .andExpect(ResponseMatchers.xpath("//ns3:Acknowledgement/ackType/TimeStamp", ns).exists())
                .andExpect(noFault())
    }

    fun resource(location: String): Resource {
        return resourceLoader.getResource(location)
    }
}
