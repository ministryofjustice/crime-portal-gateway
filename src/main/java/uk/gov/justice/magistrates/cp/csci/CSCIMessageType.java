
package uk.gov.justice.magistrates.cp.csci;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import uk.gov.justice.magistrates.cp.csci_body.MessageBodyType;
import uk.gov.justice.magistrates.generic.csci_header.MessageHeader;
import uk.gov.justice.magistrates.generic.csci_status.MessageStatus;


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
 *         &lt;element ref="{http://www.justice.gov.uk/magistrates/generic/CSCI_Header}MessageHeader"/&gt;
 *         &lt;element ref="{http://www.justice.gov.uk/magistrates/cp/CSCI_Body}MessageBody"/&gt;
 *         &lt;element ref="{http://www.justice.gov.uk/magistrates/generic/CSCI_Status}MessageStatus" minOccurs="0"/&gt;
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
    "messageHeader",
    "messageBody",
    "messageStatus"
})
@XmlRootElement(name = "CSCI_Message_Type")
public class CSCIMessageType {

    @XmlElement(name = "MessageHeader", namespace = "http://www.justice.gov.uk/magistrates/generic/CSCI_Header", required = true)
    protected MessageHeader messageHeader;
    @XmlElement(name = "MessageBody", namespace = "http://www.justice.gov.uk/magistrates/cp/CSCI_Body", required = true)
    protected MessageBodyType messageBody;
    @XmlElement(name = "MessageStatus", namespace = "http://www.justice.gov.uk/magistrates/generic/CSCI_Status")
    protected MessageStatus messageStatus;

    /**
     * Gets the value of the messageHeader property.
     * 
     * @return
     *     possible object is
     *     {@link MessageHeader }
     *     
     */
    public MessageHeader getMessageHeader() {
        return messageHeader;
    }

    /**
     * Sets the value of the messageHeader property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageHeader }
     *     
     */
    public void setMessageHeader(MessageHeader value) {
        this.messageHeader = value;
    }

    /**
     * Gets the value of the messageBody property.
     * 
     * @return
     *     possible object is
     *     {@link MessageBodyType }
     *     
     */
    public MessageBodyType getMessageBody() {
        return messageBody;
    }

    /**
     * Sets the value of the messageBody property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageBodyType }
     *     
     */
    public void setMessageBody(MessageBodyType value) {
        this.messageBody = value;
    }

    /**
     * Gets the value of the messageStatus property.
     * 
     * @return
     *     possible object is
     *     {@link MessageStatus }
     *     
     */
    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    /**
     * Sets the value of the messageStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageStatus }
     *     
     */
    public void setMessageStatus(MessageStatus value) {
        this.messageStatus = value;
    }

}
