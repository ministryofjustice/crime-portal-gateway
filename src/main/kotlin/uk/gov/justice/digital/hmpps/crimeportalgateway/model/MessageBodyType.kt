package uk.gov.justice.digital.hmpps.crimeportalgateway.model

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import uk.gov.justice.digital.hmpps.crimeportalgateway.messaging.MessageParser.Companion.GW_MSG_SCHEMA
import javax.validation.Valid
import javax.validation.constraints.NotNull

data class MessageBodyType(
    @field:Valid
    @field:NotNull
    @JacksonXmlProperty(namespace = GW_MSG_SCHEMA, localName = "GatewayOperationType")
    val gatewayOperationType: GatewayOperationType,
)
