package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import jakarta.xml.bind.JAXBContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
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
import org.springframework.xml.xsd.XsdSchemaCollection
import org.springframework.xml.xsd.commons.CommonsXsdSchemaCollection
import uk.gov.justice.magistrates.external.externaldocumentrequest.ExternalDocumentRequest
import java.io.File
import javax.xml.XMLConstants
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory

@EnableWs
@Configuration
class WebServiceConfig(
    @Value("\${soap.ws-location-uri}") private val wsLocationUri: String,
    @Value("\${soap.target-namespace}") private val targetNamespace: String,
    @Value("\${soap.xsd-file-path}") private val xsdFilePath: String,
) {
    @Bean
    fun externalDocumentXsdResource(): Resource = FileSystemResource(xsdFilePath)

    @Bean
    fun externalDocumentRequestWsdl(externalDocumentXsdResource: Resource): SimpleWsdl11Definition = SimpleWsdl11Definition(externalDocumentXsdResource)

    @Bean
    fun messageDispatcherServlet(applicationContext: ApplicationContext): ServletRegistrationBean<*>? =
        ServletRegistrationBean(
            MessageDispatcherServlet(applicationContext as WebApplicationContext).apply {
                setApplicationContext(applicationContext)
                isTransformWsdlLocations = true
            },
            "$wsLocationUri*",
        )

    @Bean
    fun messageFactory(): SaajSoapMessageFactory =
        SaajSoapMessageFactory().apply {
            setSoapVersion(SoapVersion.SOAP_12)
        }

    @Bean(name = ["ExternalDocumentRequest"])
    fun wsdl11Definition(requestSchema: XsdSchema): DefaultWsdl11Definition =
        DefaultWsdl11Definition().apply {
            setPortTypeName("WebServicePort")
            setLocationUri(wsLocationUri)
            setTargetNamespace(targetNamespace)
            setSchemaCollection(xsds())
        }

    @Bean
    fun xsds(): XsdSchemaCollection {
        val xsds =
            CommonsXsdSchemaCollection(
                ClassPathResource("xsd/cp/external/ExternalDocumentRequest.xsd"),
                ClassPathResource("xsd/generic/Acknowledgement/Acknowledgement.xsd"),
            )
        xsds.setInline(true)
        return xsds
    }

    @Bean
    fun requestSchema(externalDocumentXsdResource: Resource): XsdSchema = SimpleXsdSchema(externalDocumentXsdResource)

    @ConditionalOnProperty(value = ["soap.validate-payload"], havingValue = "true")
    @Bean
    fun validationSchema(externalDocumentXsdResource: Resource): Schema {
        val schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
        return schemaFactory.newSchema(File(xsdFilePath))
    }

    @Bean
    fun jaxbContext(): JAXBContext = JAXBContext.newInstance(ExternalDocumentRequest::class.java)
}
