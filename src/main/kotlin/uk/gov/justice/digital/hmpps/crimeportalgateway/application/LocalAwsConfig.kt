package uk.gov.justice.digital.hmpps.crimeportalgateway.application

import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Profile("local")
@Configuration
class LocalAwsConfig : BaseAwsConfig() {

    @Value("\${aws.localstack-endpoint-url}")
    lateinit var endpointUrl: String

    @Bean
    fun amazonS3LocalStackClient(): AmazonS3 {
        return AmazonS3ClientBuilder
            .standard()
            .withRegion(regionName)
            .build()
    }

    @Bean
    fun amazonSNSLocalStackClient(): AmazonSNS {
        val endpointConfiguration = AwsClientBuilder.EndpointConfiguration(endpointUrl, regionName)

        return AmazonSNSClientBuilder
            .standard()
            .withEndpointConfiguration(endpointConfiguration)
            .build()
    }
}