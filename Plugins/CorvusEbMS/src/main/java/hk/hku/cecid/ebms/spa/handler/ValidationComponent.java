package hk.hku.cecid.ebms.spa.handler;

import hk.hku.cecid.piazza.commons.module.Component;
import jakarta.xml.soap.AttachmentPart;
import jakarta.xml.soap.SOAPException;
import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.IOException;
@Slf4j
public class ValidationComponent extends Component {

	private static final String SCHEMA_LANGUAGE = "http://www.w3.org/2001/XMLSchema";

	private SchemaFactory factory;
	private File schemaFile;
	private Schema xsdScheme;

	@Override
	protected void init() throws Exception {
		super.init();
		factory = SchemaFactory.newInstance(SCHEMA_LANGUAGE);
		schemaFile = new File(getSchemaLocation());
		xsdScheme = factory.newSchema(schemaFile);
	}

	public void validate(AttachmentPart attachment) throws SOAPException {

		try {
			Validator validator = xsdScheme.newValidator();
			Source source = new StreamSource(attachment.getRawContent());
			validator.validate(source);
		} catch (SAXException e) {
			throw new InvalidAttachmentException(e);
		} catch (IOException e) {
			log.error("IOException", e);
		}

	}

	protected String getSchemaLocation() {
		return getParameters().getProperty("schemaLocation");
	}

}
