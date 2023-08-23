package hk.hku.cecid.ebms.spa.client.jms;

import lombok.extern.slf4j.Slf4j;

import javax.jms.Connection;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
@Slf4j
public class JMSExceptionListener implements ExceptionListener {
	private Connection jmsConnection;

	public JMSExceptionListener(Connection jmsConnecion) {
		this.jmsConnection = jmsConnection;
	}

	public void onException(JMSException e) {
		try {
			log.warn("JMSException", e);
			if (jmsConnection != null) {
				jmsConnection.stop();
				jmsConnection.start();
			}
		} catch (JMSException e1) {
			log.error("JMSException", e1);
			e1.printStackTrace();
		}
	}

}