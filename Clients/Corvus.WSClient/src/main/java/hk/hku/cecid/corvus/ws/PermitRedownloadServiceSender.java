package hk.hku.cecid.corvus.ws;

import hk.hku.cecid.corvus.ws.data.PermitRedownloadData;
import hk.hku.cecid.piazza.commons.data.Data;
import hk.hku.cecid.piazza.commons.soap.SOAPSender;
import hk.hku.cecid.piazza.commons.util.SOAPUtilities;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;
import lombok.extern.slf4j.Slf4j;

import java.net.MalformedURLException;
import java.util.Date;

;

@Slf4j
public abstract class PermitRedownloadServiceSender 
	extends SOAPSender{
	
	/**
	 * The start time of sending. It is used for sender which
	 * want to track how long it take to execute.
	 */
	private long startTime = 0;

	/**
	 * The end time of sending. It is used for sender which
	 * want to track how long it take to execute.
	 */
	private long endTime = 0;
	
	/**
	 * Explicit Constructor. 
	 *
	 * @param m			The message properties including how many message need to 
	 * 					be sent and some performance parameter.
	 */
	public PermitRedownloadServiceSender(Data m){
		super(m);
		this.setLoopTimes(1);
	}	
	
	/**
	 *  Initialize Sender and Construct Message Request
	 */
	public void initializeMessage() throws SOAPException{
		if (!(this.properties instanceof PermitRedownloadData))
			throw new ClassCastException("Invalid class data");	
		
		PermitRedownloadData data = (PermitRedownloadData) this.properties;
		this.setServiceEndPoint(data.getEndpoint());	
		
		this.addRequestElementText("messageId", data.getTargetMessageId(), NS_PREFIX, getNSURI());
	}
	
	/**
	 * Record the message id. 
	 */
	public void onResponse() throws Exception{
		String msgId = null;

		SOAPElement elem = 
			SOAPUtilities.getElement(this.response, "messageId", getNSURI(), 0);
		
		if (elem != null)
			msgId = elem.getValue();
		
		if (this.log != null)	
			log.info("Message Id Reset: " + msgId);
	}	
	
	/**
	 * This method should only be called inside {@link #onResponse()}.
	 * because the response object will be deleted upon each ws call.
	 * 
	 * @param tagname	The tag name of element to be retrieved.
	 * @param nsURI		The namespace URI.
	 * @param whichOne	The nth child element to be returned.	 
	 * 
	 * @return The element text in the tagname specified.
	 */
	public String getResponseElementText(String tagname
										,String nsURI
										,int	whichOne) throws SOAPException{
		SOAPElement elem = SOAPUtilities.getElement(this.response, tagname, getNSURI(), whichOne);
		if (elem != null)
			return elem.getValue();
		return "";
	}
	
	/** 
	 * [@EVENT] This method is invoked when the sender begins to execute the 
	 * run method.  
	 */
	public void onStart(){
		this.startTime = new Date().getTime();		
		
		if (this.log != null){
			// Log all information for this sender.
			log.info("Permit Redownload Request Sender init at " + new Date().toString());
			log.info("----------------------------------------------------");
			log.info("Configuration Data using: ");
			log.info("----------------------------------------------------");
			if (this.properties != null){
				log.info(this.properties.toString());
			}			
			log.info("----------------------------------------------------");
		}		
		try{
			this.initializeMessage();
			this.setRequestDirty(false);
		}catch(Exception e){
			if (this.log != null)
				log.info("Unable to initialize the SOAP Message");
			this.onError(e);
		}
	}
	
	/**
	 * [@EVENT] This method is invoked when the sending execution is ended. 
	 */
	public void onEnd(){
		this.endTime = new Date().getTime();
	}	
	
	/**
	 * [@EVENT] Log all known exceptions and stack trace.
	 * 
	 * @param t 	The exception encountered.
	 */
	public void onError(Throwable t){
		String msg = t.getMessage();
		
		if (this.log != null){
			if (t instanceof MalformedURLException){
				this.log.error("Could not find the URL: " + this.getServiceEndPoint(), t);
			} else if (t instanceof UnsupportedOperationException){
				this.log.error("Unsupported SOAP class and web services: " + msg, t);
			} else if (t instanceof NullPointerException){
				this.log.error("Null Pointer Exception", t);
			}else if (t instanceof SOAPException){
				this.log.error("Could not send the SOAP message: " + msg, t);
			}

			
		} else {
			t.printStackTrace();
		}
	}	
	
	/**
	 * Get how long it tasks for the sender to do it's tasks.
	 * 
	 * @return The times for the task from start to end in milleseconds.
	 */
	public long getElapsedTime(){
		return (this.endTime - this.startTime);
	}	
	
	/** 
	 * @return Return the start time of the sending process.
	 */
	public long getStartTime(){
		return this.startTime;
	}
	
	/**
	 * @return Return the end time of the sender process.
	 */
	public long getEndTime(){
		return this.endTime;
	}
	
	protected abstract String getNSURI();
}
