package org.jentrata.spa.integration.module;

import hk.hku.cecid.piazza.commons.module.Component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.util.Map;

/**
 * Created by aaronwalker on 8/07/2016.
 */
@Slf4j
public class SpringBootstrapComponent extends Component {

    private ClassPathXmlApplicationContext parent;

    @Override
    protected void init() throws Exception {
        super.init();
        log.debug("Camel Bootstrap");
        parent = new ClassPathXmlApplicationContext();
        parent.refresh();
        log.info("Spring Root parent is active:" + parent.isActive() + " and running " + parent.isRunning());
    }

    public ApplicationContext deployApplicationContext(String contextFile,Map<String,Object> beans) {
        FileSystemXmlApplicationContext newContext = new FileSystemXmlApplicationContext(parent);
        newContext.refresh();
        for(String key: beans.keySet()) {
            newContext.getBeanFactory().registerSingleton(key,beans.get(key));
        }
        newContext.setConfigLocation(contextFile);
        newContext.refresh();
        return newContext;
    }

    public void registerBean(String id, Object bean) {
        parent.getBeanFactory().registerSingleton(id,bean);
    }


    public ApplicationContext getSpringContext() {
        return parent;
    }

    public void shutdown() {
        if(parent != null) {
            parent.stop();
        }
    }


}

