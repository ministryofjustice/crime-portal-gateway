
package uk.gov.justice.magistrates.cp.csci_body;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import uk.gov.justice.magistrates.cp.gatewaymessageschema.GatewayOperationType;


/**
 * <p>Java class for MessageBodyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="MessageBodyType"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element ref="{http://www.justice.gov.uk/magistrates/cp/GatewayMessageSchema}GatewayOperationType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MessageBodyType", propOrder = {
    "gatewayOperationType"
})
public class MessageBodyType {

    @XmlElement(name = "GatewayOperationType", namespace = "http://www.justice.gov.uk/magistrates/cp/GatewayMessageSchema", required = true)
    protected GatewayOperationType gatewayOperationType;

    /**
     * Gets the value of the gatewayOperationType property.
     * 
     * @return
     *     possible object is
     *     {@link GatewayOperationType }
     *     
     */
    public GatewayOperationType getGatewayOperationType() {
        return gatewayOperationType;
    }

    /**
     * Sets the value of the gatewayOperationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link GatewayOperationType }
     *     
     */
    public void setGatewayOperationType(GatewayOperationType value) {
        this.gatewayOperationType = value;
    }

}
