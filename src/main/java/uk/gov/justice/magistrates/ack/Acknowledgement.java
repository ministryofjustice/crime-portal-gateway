
package uk.gov.justice.magistrates.ack;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Ack" type="{http://www.justice.gov.uk/magistrates/ack}AckType"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "ack"
})
@XmlRootElement(name = "Acknowledgement")
public class Acknowledgement {

    @XmlElement(name = "Ack", required = true)
    protected AckType ack;

    /**
     * Gets the value of the ack property.
     * 
     * @return
     *     possible object is
     *     {@link AckType }
     *     
     */
    public AckType getAck() {
        return ack;
    }

    /**
     * Sets the value of the ack property.
     * 
     * @param value
     *     allowed object is
     *     {@link AckType }
     *     
     */
    public void setAck(AckType value) {
        this.ack = value;
    }

}
