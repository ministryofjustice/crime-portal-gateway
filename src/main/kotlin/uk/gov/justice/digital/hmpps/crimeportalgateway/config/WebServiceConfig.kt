package uk.gov.justice.digital.hmpps.crimeportalgateway.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.Resource
import org.springframework.web.context.WebApplicationContext
import org.springframework.ws.config.annotation.EnableWs
import org.springframework.ws.soap.SoapVersion
import org.springframework.ws.soap.saaj.SaajSoapMessageFactory
import org.springframework.ws.transport.http.MessageDispatcherServlet
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition
import org.springframework.xml.xsd.SimpleXsdSchema
import org.springframework.xml.xsd.XsdSchema

@EnableWs
@Configuration
class WebServiceConfig(@Value("\${soap.ws-location-uri}") private val wsLocationUri: String,
                        @Value("\${soap.target-namespace}") private val targetNamespace : String,
                        @Value("\${soap.xsd-file-path}") private val xsdFilePath : String) {

    @Bean
    fun externalDocumentXsdResource(): Resource {
        return FileSystemResource(xsdFilePath);
    }

    @Bean
    fun externalDocumentRequestWsdl(externalDocumentXsdResource: Resource): SimpleWsdl11Definition {
        return SimpleWsdl11Definition(externalDocumentXsdResource)
    }

    @Bean
    fun messageDispatcherServlet(applicationContext: ApplicationContext): ServletRegistrationBean<*>? {
        val servlet = MessageDispatcherServlet(applicationContext as WebApplicationContext?)
        servlet.isTransformWsdlLocations = true
        return ServletRegistrationBean(servlet, "$wsLocationUri*")
    }

    @Bean
    fun messageFactory(): SaajSoapMessageFactory {
        val messageFactory = SaajSoapMessageFactory()
        messageFactory.setSoapVersion(SoapVersion.SOAP_12)
        return messageFactory
    }

    @Bean(name = ["ExternalDocumentRequest"])
    fun wsdl11Definition(requestSchema: XsdSchema): DefaultWsdl11Definition? {
        val wsdl11Definition = DefaultWsdl11Definition()
        wsdl11Definition.setPortTypeName("WebServicePort")
        wsdl11Definition.setLocationUri(wsLocationUri)
        wsdl11Definition.setTargetNamespace(targetNamespace)
        wsdl11Definition.setSchema(requestSchema)
        return wsdl11Definition
    }

    @Bean
    fun requestSchema(externalDocumentXsdResource: Resource): XsdSchema {
        return SimpleXsdSchema(externalDocumentXsdResource)
    }

}
