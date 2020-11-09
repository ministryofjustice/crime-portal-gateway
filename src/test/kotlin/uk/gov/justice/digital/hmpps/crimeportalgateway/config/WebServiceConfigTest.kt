package uk.gov.justice.digital.hmpps.crimeportalgateway.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.web.context.WebApplicationContext
import org.springframework.ws.client.core.WebServiceTemplate
import org.springframework.ws.config.annotation.EnableWs
import org.springframework.ws.config.annotation.WsConfigurerAdapter
import org.springframework.ws.server.EndpointInterceptor
import org.springframework.ws.soap.SoapVersion
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor
import org.springframework.ws.soap.security.wss4j2.callback.KeyStoreCallbackHandler
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean
import org.springframework.ws.transport.http.MessageDispatcherServlet
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition
import org.springframework.xml.xsd.SimpleXsdSchema
import org.springframework.xml.xsd.XsdSchema
import java.io.IOException
import javax.xml.soap.MessageFactory
import javax.xml.soap.SOAPConstants

@EnableWs
@TestConfiguration
//@Profile("test")
class WebServiceConfigTest(@Value("\${soap.ws-location-uri}") private val wsLocationUri: String,
                           @Value("\${soap.target-namespace}") private val targetNamespace : String)
    : WsConfigurerAdapter() {

    override fun addInterceptors(interceptors: MutableList<EndpointInterceptor?>) {
        interceptors.add(SoapHeaderAddressInterceptor())
    }

}
