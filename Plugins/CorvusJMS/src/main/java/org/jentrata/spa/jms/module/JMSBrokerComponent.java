package org.jentrata.spa.jms.module;

import hk.hku.cecid.piazza.commons.module.Component;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

/**
 * Creates an ActiveMQ Message Broker
 */
@Slf4j
public class JMSBrokerComponent extends Component {

    private BrokerService brokerService;

    @Override
    protected void init() throws Exception {
        super.init();
        log.debug("Starting JMS Broker:" + getBrokerUri());
        brokerService = BrokerFactory.createBroker(getBrokerUri(),true);
        brokerService.setUseShutdownHook(false);
        log.info("Started JMS Broker");
    }

    public BrokerService getBrokerService() {
        return brokerService;
    }

    public String getBrokerUri() {
        return getParameters().getProperty("brokerUri");
    }

    public void shutdownBroker() {
        if(brokerService != null && brokerService.isStarted()) {
            try {
                log.info("Stopping JMS Broker:" + getBrokerUri());
                brokerService.stop();
            } catch (Exception e) {
                log.warn("unable to stop JMS Broker:" + e.getMessage());
                log.debug("",e);
            }
        } else {
            log.warn("JMS Broker already stopped");
        }
    }
}
