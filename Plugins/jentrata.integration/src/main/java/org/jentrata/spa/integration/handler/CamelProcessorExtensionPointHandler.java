package org.jentrata.spa.integration.handler;

import hk.hku.cecid.piazza.commons.spa.Extension;
import hk.hku.cecid.piazza.commons.spa.ExtensionPointIteratedHandler;
import hk.hku.cecid.piazza.commons.spa.PluginException;
import lombok.extern.slf4j.Slf4j;
import org.jentrata.spa.integration.IntegrationPluguin;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by aaronwalker on 11/07/2016.
 */
@Slf4j
public class CamelProcessorExtensionPointHandler  extends ExtensionPointIteratedHandler {
    @Override
    public void processExtension(Extension extension) throws PluginException {
        String deployLocation = extension.getParameter("deployLocation");
        try {
            Properties config = new Properties();
            config.putAll(extension.getParameters());

            if (deployLocation != null) {
                deployCamelContext(deployLocation, config);
            } else {
                throw new PluginException("Unable to deploy camel integration: deployLocation defined");
            }
        } catch (Exception e) {
            throw new PluginException("Unable to deploy camel context extension: " + deployLocation, e);
        }
    }

    public void deployCamelContext(String deployLocation, Properties config) throws Exception {
        log.debug("Adding camel context handler:" + deployLocation);
        Map<String, Object> beans = new HashMap<String, Object>();
        beans.put(config.getProperty("id","config"),config);
        ApplicationContext context = IntegrationPluguin.getInstance().getSpringBootstrapComponent().deployApplicationContext(deployLocation,beans);
        log.info("Added camel context:" + deployLocation);
    }

}
