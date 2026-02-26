
package uk.gov.justice.magistrates.cp.csci_body;

import javax.xml.namespace.QName;
import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the uk.gov.justice.magistrates.cp.csci_body package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _MessageBody_QNAME = new QName("http://www.justice.gov.uk/magistrates/cp/CSCI_Body", "MessageBody");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: uk.gov.justice.magistrates.cp.csci_body
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MessageBodyType }
     * 
     */
    public MessageBodyType createMessageBodyType() {
        return new MessageBodyType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MessageBodyType }{@code >}
     * 
     * @param value
     *     Java instance representing xml element's value.
     * @return
     *     the new instance of {@link JAXBElement }{@code <}{@link MessageBodyType }{@code >}
     */
    @XmlElementDecl(namespace = "http://www.justice.gov.uk/magistrates/cp/CSCI_Body", name = "MessageBody")
    public JAXBElement<MessageBodyType> createMessageBody(MessageBodyType value) {
        return new JAXBElement<MessageBodyType>(_MessageBody_QNAME, MessageBodyType.class, null, value);
    }

}
