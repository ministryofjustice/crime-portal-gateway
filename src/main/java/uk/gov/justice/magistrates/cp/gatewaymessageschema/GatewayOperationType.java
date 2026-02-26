
package uk.gov.justice.magistrates.cp.gatewaymessageschema;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import uk.gov.justice.magistrates.ack.Acknowledgement;
import uk.gov.justice.magistrates.external.externaldocumentrequest.ExternalDocumentRequest;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;element ref="{http://www.justice.gov.uk/magistrates/ack}Acknowledgement"/&gt;
 *         &lt;element ref="{http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest}ExternalDocumentRequest"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "acknowledgement",
    "externalDocumentRequest"
})
@XmlRootElement(name = "GatewayOperationType")
public class GatewayOperationType {

    @XmlElement(name = "Acknowledgement", namespace = "http://www.justice.gov.uk/magistrates/ack")
    protected Acknowledgement acknowledgement;
    @XmlElement(name = "ExternalDocumentRequest", namespace = "http://www.justice.gov.uk/magistrates/external/ExternalDocumentRequest")
    protected ExternalDocumentRequest externalDocumentRequest;

    /**
     * Gets the value of the acknowledgement property.
     * 
     * @return
     *     possible object is
     *     {@link Acknowledgement }
     *     
     */
    public Acknowledgement getAcknowledgement() {
        return acknowledgement;
    }

    /**
     * Sets the value of the acknowledgement property.
     * 
     * @param value
     *     allowed object is
     *     {@link Acknowledgement }
     *     
     */
    public void setAcknowledgement(Acknowledgement value) {
        this.acknowledgement = value;
    }

    /**
     * Gets the value of the externalDocumentRequest property.
     * 
     * @return
     *     possible object is
     *     {@link ExternalDocumentRequest }
     *     
     */
    public ExternalDocumentRequest getExternalDocumentRequest() {
        return externalDocumentRequest;
    }

    /**
     * Sets the value of the externalDocumentRequest property.
     * 
     * @param value
     *     allowed object is
     *     {@link ExternalDocumentRequest }
     *     
     */
    public void setExternalDocumentRequest(ExternalDocumentRequest value) {
        this.externalDocumentRequest = value;
    }

}
