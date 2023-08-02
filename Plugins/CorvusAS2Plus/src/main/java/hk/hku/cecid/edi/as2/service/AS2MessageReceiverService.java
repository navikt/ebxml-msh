/* 
 * Copyright(c) 2005 Center for E-Commerce Infrastructure Development, The
 * University of Hong Kong (HKU). All Rights Reserved.
 *
 * This software is licensed under the GNU GENERAL PUBLIC LICENSE Version 2.0 [1]
 * 
 * [1] http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt
 */

package hk.hku.cecid.edi.as2.service;

import hk.hku.cecid.edi.as2.AS2PlusProcessor;
import hk.hku.cecid.edi.as2.dao.MessageDAO;
import hk.hku.cecid.edi.as2.dao.MessageDVO;
import hk.hku.cecid.edi.as2.dao.RepositoryDAO;
import hk.hku.cecid.edi.as2.dao.RepositoryDVO;
import hk.hku.cecid.edi.as2.pkg.AS2Message;
import hk.hku.cecid.edi.as2.util.AS2Util;
import hk.hku.cecid.piazza.commons.activation.ByteArrayDataSource;
import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.io.IOHandler;
import hk.hku.cecid.piazza.commons.soap.SOAPFaultException;
import hk.hku.cecid.piazza.commons.soap.SOAPRequestException;
import hk.hku.cecid.piazza.commons.soap.SOAPResponse;
import hk.hku.cecid.piazza.commons.soap.WebServicesAdaptor;
import hk.hku.cecid.piazza.commons.soap.WebServicesRequest;
import hk.hku.cecid.piazza.commons.soap.WebServicesResponse;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import jakarta.xml.soap.AttachmentPart;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPMessage;

import org.w3c.dom.Element;

/**
 * AS2MessageReceiverListService
 * 
 * @author Donahue Sze
 *  
 */
public class AS2MessageReceiverService extends WebServicesAdaptor {

    public void serviceRequested(WebServicesRequest request,
            WebServicesResponse response) throws SOAPRequestException,
            DAOException {

        Element[] bodies = request.getBodies();
        String messageId = getText(bodies, "messageId");

        if (messageId == null) {
            throw new SOAPFaultException(SOAPFaultException.SOAP_FAULT_CLIENT,
                    "Missing request information");
        }

        AS2PlusProcessor.getInstance().getLogger().info("Message Receiver received download request - Message ID: "
                + messageId);

        SOAPResponse soapResponse = (SOAPResponse) response.getTarget();
        SOAPMessage soapResponseMessage = soapResponse.getMessage();

        MessageDAO messageDao = (MessageDAO) AS2PlusProcessor.getInstance().getDAOFactory()
                .createDAO(MessageDAO.class);
        MessageDVO messageDvo = (MessageDVO) messageDao.createDVO();
        messageDvo.setMessageId(messageId);
        messageDvo.setMessageBox(MessageDVO.MSGBOX_IN);
        messageDvo.setAs2From("%");
        messageDvo.setAs2To("%");
        messageDvo.setStatus(MessageDVO.STATUS_PROCESSED);

        List messagesList = messageDao.findMessagesByHistory(messageDvo,
                1, 0);
        Iterator messagesIterator = messagesList.iterator();

        while (messagesIterator.hasNext()) {
        	
            MessageDVO targetMessageDvo = (MessageDVO) messagesIterator.next();
            
            RepositoryDAO repoDao = 
            	(RepositoryDAO) AS2PlusProcessor.getInstance().getDAOFactory().createDAO(RepositoryDAO.class);
            RepositoryDVO repoDvo = (RepositoryDVO) repoDao.createDVO();
            repoDvo.setMessageId(targetMessageDvo.getMessageId());
            repoDvo.setMessageBox(targetMessageDvo.getMessageBox());
            
            if(!repoDao.retrieve(repoDvo)){
            	throw new DAOException("Unable to collect payload content in databse.");
            }
            
            ByteArrayInputStream ins = new ByteArrayInputStream(repoDvo.getContent());
            try {
//				MimeBodyPart contentPart = new MimeBodyPart(new InternetHeaders(ins), repoDvo.getContent());
            	AS2Message as2Msg = new AS2Message(ins);
            	
				// Retrieve Filename from MIME Header            	
	            String[] values = as2Msg.getBodyPart().getHeader("Content-Disposition");
	            String filename = AS2Util.getFileNameFromMIMEHeader(values);
	            
	            //Debug Message
//	            AS2PlusProcessor.getInstance().getLogger().debug(
//	            		"AS2Plus Filename found? " + (filename ==null?"Not found":filename));
				
				//Check if compression is needed
				DataSource ds = null;
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				if (getParameters().getProperty("is_compress")
                        .equals("true")) {
                    DeflaterOutputStream dos = new DeflaterOutputStream(
                            baos);
                    IOHandler.pipe(as2Msg.getBodyPart().getInputStream(), dos);
                    dos.finish();
                    ds = new ByteArrayDataSource(baos.toByteArray(),
                            "application/deflate");
                } else {
                    IOHandler.pipe(as2Msg.getBodyPart().getInputStream(), baos);
                    ds = new ByteArrayDataSource(baos.toByteArray(),
                    		as2Msg.getBodyPart().getContentType());
                }
				DataHandler dh = new DataHandler(ds);
				
				//Create AttachmentPart and add to SOAP Message
				AttachmentPart attachmentPart = soapResponseMessage.createAttachmentPart();
				attachmentPart.setContentId(as2Msg.getBodyPart().getContentID());
				attachmentPart.setContentType(as2Msg.getBodyPart().getContentType());
				
				try{
					//Set Filename if filename value is valid and received
					if(filename != null && !filename.trim().equalsIgnoreCase("")){

						//Validate Filename value
						char[] filenameChars = filename.toCharArray();
		            	for(char charValue : filenameChars){
		            		if(charValue >= 128){
		            			 throw new Exception(
		            					 "Filename contains Non-ASCII characters in Message: "+as2Msg.getMessageID());
		            		}
		            	}

						attachmentPart.addMimeHeader(
								"Content-Disposition", "attachment; filename="+filename);
					}
				}catch(Exception exp){
					//If there is any abnormal value of MimeHeader 
					// and caused Exception the filename will be replaced with default value.
					 AS2PlusProcessor.getInstance().getLogger().error(
	                         "Default Filename Value applied on Message["+as2Msg.getMessageID()+"]", exp);
					attachmentPart.addMimeHeader(
							"Content-Disposition", "attachment; filename="+ "as2." +as2Msg.getMessageID()+".Payload.0");
				}
				
				attachmentPart.setDataHandler(dh);
				soapResponseMessage.addAttachmentPart(attachmentPart);
				
			    targetMessageDvo.setStatus(MessageDVO.STATUS_DELIVERED);
			    messageDao.persist(targetMessageDvo);
            
            } catch (Exception e) {
            	 AS2PlusProcessor.getInstance().getLogger().error(
                         "Error in collecting message", e);
			}
        }

        generateReply(response, soapResponseMessage
                .countAttachments()>0);
    }

    private void generateReply(WebServicesResponse response,
            boolean isReturned) throws SOAPRequestException {
        try {
            SOAPElement responseElement = createText(
                    "hasMessage", Boolean.toString(isReturned),
                    "http://service.as2.edi.cecid.hku.hk/");
            response.setBodies(new SOAPElement[] { responseElement });
        } catch (Exception e) {
            throw new SOAPRequestException("Unable to generate reply message",
                    e);
        }
    }

    protected boolean isCacheEnabled() {
        return false;
    }

    /**
     * @param parameter
     * @return
     */
	/*
    private String checkStarAndConvertToPercent(String parameter) {
        if (parameter.equals("")) {
            return new String("%");
        }
        return parameter.replace('*', '%');
    }
	*/
}