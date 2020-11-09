package uk.gov.justice.digital.hmpps.crimeportalgateway.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.ws.config.annotation.WsConfigurerAdapter
import org.springframework.ws.server.EndpointInterceptor
import org.springframework.ws.soap.security.wss4j2.Wss4jSecurityInterceptor
import org.springframework.ws.soap.security.wss4j2.callback.KeyStoreCallbackHandler
import org.springframework.ws.soap.security.wss4j2.support.CryptoFactoryBean
import java.io.IOException

@Profile("prod", "preprod", "dev", "local")
@Configuration
class WebServiceSecurityConfig(@Value("\${soap.keystore-password}") private val keystorePassword: String,
                               @Value("\${soap.inbound-encrypt-actions}") private val inboundActions: String,
                               @Value("\${soap.keystore-file-path}") private val keystoreFilePath: String)
    : WsConfigurerAdapter() {

    override fun addInterceptors(interceptors: MutableList<EndpointInterceptor?>) {
        interceptors.add(SoapHeaderAddressInterceptor())
        interceptors.add(securityInterceptor());
    }

    @Bean
    fun securityCallbackHandler(): KeyStoreCallbackHandler {
        val callbackHandler = KeyStoreCallbackHandler()
        callbackHandler.setPrivateKeyPassword(keystorePassword)
        return callbackHandler
    }

    @Bean
    @Throws(IOException::class)
    fun getCryptoFactoryBean(): CryptoFactoryBean {
        val cryptoFactoryBean = CryptoFactoryBean()
        cryptoFactoryBean.setKeyStorePassword(keystorePassword)
        cryptoFactoryBean.setKeyStoreLocation(FileSystemResource(keystoreFilePath))
        return cryptoFactoryBean
    }

    @Bean
    @Throws(Exception::class)
    fun securityInterceptor(): Wss4jSecurityInterceptor {
        val securityInterceptor = Wss4jSecurityInterceptor()

        // validate incoming request
        securityInterceptor.setValidationActions(inboundActions)
        securityInterceptor.setValidationSignatureCrypto(getCryptoFactoryBean().getObject())
        securityInterceptor.setValidationDecryptionCrypto(getCryptoFactoryBean().getObject())
        securityInterceptor.setValidationCallbackHandler(securityCallbackHandler())

        // encrypt and sign the response
        securityInterceptor.setSecurementEncryptionUser("client-public")
        securityInterceptor.setSecurementEncryptionParts("{Content}{http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest}")
        securityInterceptor.setSecurementEncryptionCrypto(getCryptoFactoryBean().getObject())
        // sign the response
//        securityInterceptor.setSecurementActions("Signature Encrypt")
//        securityInterceptor.setSecurementUsername("server")
//        securityInterceptor.setSecurementPassword("changeit")
//        securityInterceptor.setSecurementSignatureCrypto(getCryptoFactoryBean().getObject())
        return securityInterceptor
    }

}