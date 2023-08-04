package org.jentrata.validation;

import java.util.Properties;

import jakarta.xml.soap.AttachmentPart;

public interface Validator {
    
    public void init(Properties config);
    public void validate(AttachmentPart payload) throws ValidationException;

}
