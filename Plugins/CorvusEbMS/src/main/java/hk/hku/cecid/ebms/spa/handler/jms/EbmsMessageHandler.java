/**
 * 
 */
package hk.hku.cecid.ebms.spa.handler.jms;

import hk.hku.cecid.ebms.pkg.EbxmlMessage;
import hk.hku.cecid.ebms.pkg.MessageHeader;
import hk.hku.cecid.ebms.spa.EbmsProcessor;
import hk.hku.cecid.ebms.spa.EbmsUtility;
import hk.hku.cecid.ebms.spa.handler.MessageServiceHandler;
import hk.hku.cecid.ebms.spa.listener.EbmsRequest;
import hk.hku.cecid.ebms.spa.util.PartnershipDAOHelper;
import hk.hku.cecid.piazza.commons.dao.DAOException;
import hk.hku.cecid.piazza.commons.message.Message;
import hk.hku.cecid.piazza.commons.message.MessageHandler;
import hk.hku.cecid.piazza.commons.util.Generator;
import hk.hku.cecid.piazza.commons.util.Logger;

import java.util.List;
import java.util.Map;

import jakarta.activation.DataHandler;


/**
 * @author aaronwalker
 *
 */
public class EbmsMessageHandler implements MessageHandler {

    public void onMessage(Message message) {
    
        log().debug("got message:" + message.getSource().toString());
        try {
            EbmsRequest ebmsRequest = buildEbmsRequest(message);
            getMSH().processOutboundMessage(ebmsRequest, null);
        } catch (Exception e) {
            log().error("Failed to process outbound message: " + e);
            log().debug("",e);
            throw new RuntimeException(e);
        }
    }
    
    protected MessageServiceHandler getMSH() {
        return MessageServiceHandler.getInstance();
    }

    private EbmsRequest buildEbmsRequest(Message message) throws Exception {
        EbmsRequest request = new EbmsRequest();
        
        //extract delivery info from the message
        Map<String,Object> header = message.getHeader();
        String cpaId = asString(header,"cpaId");
        String conversationId = asString(header,"conversationId");
        String serviceType = asString(header, "serviceType");
        String service = asString(header, "service");
        String action = asString(header,"action");
        Integer timeToLiveOffset = asInt(header, "timeToLiveOffset");
        String ebxmlMessageId = asString(header,"ebxmlMessageId");
        String refToMessageId = asString(header,"refToMessageId");
        String [] toPartyIds = asStringArray(header,"toPartyId");
        String [] toPartyIdTypes = asStringArray(header, "toPartyType");
        String  toPartyRole = asString(header, "toPartyRole");
        String [] fromPartyIds = asStringArray(header,"fromPartyId");
        String [] fromPartyIdTypes = asStringArray(header, "fromPartyType");
        String  fromPartyRole = asString(header, "fromPartyRole");
        String [] payloadContentIds = asStringArray(header,"payload-contentId");
        String [] payloadContentTypes = asStringArray(header,"payload-contentType");
        
        //check if there is a valid registered channel for this message
        if(!checkValidChannel(cpaId, service, action)) {
            throw new RuntimeException("No registered sender channel");
        }

        //create the ebxml message
        EbxmlMessage ebxml = new EbxmlMessage();
        MessageHeader ebxmlHeader = ebxml.addMessageHeader();

        addFromParty(ebxmlHeader,fromPartyIds,fromPartyIdTypes);
        if(fromPartyRole != null && !fromPartyRole.isEmpty()) {
            ebxmlHeader.setFromRole(fromPartyRole);
        }

        addToParty(ebxmlHeader,toPartyIds,toPartyIdTypes);
        if(toPartyRole != null && !toPartyRole.isEmpty()) {
            ebxmlHeader.setToRole(toPartyRole);
        }
        
        //These methods need to be call in this exact order
        ebxmlHeader.setCpaId(cpaId);
        ebxmlHeader.setConversationId(conversationId);
        ebxmlHeader.setService(service);
        ebxmlHeader.setAction(action);        
        
        if (serviceType != null && !serviceType.equals("")) {
            ebxmlHeader.setServiceType(serviceType);
        }

        if(ebxmlMessageId == null) {
            ebxmlMessageId = Generator.generateMessageID();
        }
        ebxmlHeader.setMessageId(ebxmlMessageId);
        log().info("ebXML message id: " + ebxmlMessageId);
        
        if(refToMessageId != null) {
            ebxmlHeader.setRefToMessageId(refToMessageId);
        }
        
        ebxmlHeader.setTimestamp(EbmsUtility.getCurrentUTCDateTime());
        
        if(timeToLiveOffset != null) {
            ebxmlHeader.setTimeToLive(EbmsUtility.applyTimeToLiveOffset(timeToLiveOffset));
        }
        
        log().info("Outbound payload received - cpaId: " + cpaId
                + ", messageId: "     + ebxmlMessageId 
                + ", service: "     + service 
                + ", serviceType:"  + serviceType               
                + ", action: "      + action 
                + ", convId: "      + conversationId 
                + ", fromPartyId: " + fromPartyIds
                + ", fromPartyType: " + fromPartyIdTypes 
                + ", toPartyId: "     + toPartyIds
                + ", toPartyType: " + toPartyIdTypes
                + ", refToMessageId: " + refToMessageId
                + ", timeToLiveOffset: " + timeToLiveOffset);

        //If no payloadIds then assume the default Payload-0
        //If no contentIds then assume text/xml
        if(payloadContentIds == null || payloadContentTypes == null) {
            attachPayloads(ebxml,message.getPayloads());
        } else {
            attachPayloads(ebxml,payloadContentIds,payloadContentTypes,message.getPayloads());
        }
        request.setSource(message);
        request.setMessage(ebxml);
        return request;
    }

    protected boolean checkValidChannel(String cpaId, String service, String action) throws DAOException {
        return PartnershipDAOHelper.isChannelRegistered(cpaId, service, action);
    }
    
    protected Logger log() {
        return EbmsProcessor.core.log;
    }

    private void attachPayloads(EbxmlMessage ebxml, List<byte[]> payloads) throws Exception {
        
        String [] payloadIds = {"Payload-0"};
        String [] contentIds = {"text/xml; charset=UTF-8"};
        attachPayloads(ebxml,payloadIds,contentIds,payloads);
    }
    
    private void attachPayloads(EbxmlMessage ebxml, String [] payloadIds, String [] contentIds, List<byte[]> payloads) throws Exception {
        int i=0;
        for(byte [] payload:payloads) {
            ebxml.addPayloadContainer(new DataHandler(new String(payload),contentIds[i]), payloadIds[i], null);
            i++;
        }
    }
    
    private void addToParty(MessageHeader ebxmlHeader, String[] toPartyIds, String[] toPartyIdTypes) throws Exception {
        for(int i=0;i<toPartyIds.length;i++) {
            ebxmlHeader.addToPartyId(toPartyIds[i], toPartyIdTypes[i]);
        }
    }

    private void addFromParty(MessageHeader ebxmlHeader, String[] fromPartyIds, String[] fromPartyIdTypes) throws Exception {
        for(int i=0;i<fromPartyIds.length;i++) {
            ebxmlHeader.addFromPartyId(fromPartyIds[i], fromPartyIdTypes[i]);
        }        
    }

    private String[] asStringArray(Map<String, Object> map, String key) {
        String value = asString(map, key);
        if(value != null) {
            return value.split(",");
        }
        return null;
    }

    private String asString(Map<String,Object> map, String key) {
        Object o = map.get(key);
        if(o != null) {
            return o.toString();
        }
        return null;
    }
    
    private Integer asInt(Map<String, Object> map, String key) {
        Object o = map.get(key);
        if(o != null) {
            try {
                return Integer.parseInt(o.toString());
            } catch (NumberFormatException ex) {
                log().warn("unable to parse an integer " + key + " from map " + map);
            }
        }
        return null;
    }

}
