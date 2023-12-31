//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.08.09 at 10:45:26 AM EST 
//


package org.jentrata.ebxml.cpa;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlIDREF;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SecurityDetailsRef.type complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SecurityDetailsRef.type">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="securityId" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SecurityDetailsRef.type")
public class SecurityDetailsRefType {

    @XmlAttribute(name = "securityId", namespace = "http://www.oasis-open.org/committees/ebxml-cppa/schema/cpp-cpa-2_0.xsd", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object securityId;

    /**
     * Gets the value of the securityId property.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getSecurityId() {
        return securityId;
    }

    /**
     * Sets the value of the securityId property.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setSecurityId(Object value) {
        this.securityId = value;
    }

}
