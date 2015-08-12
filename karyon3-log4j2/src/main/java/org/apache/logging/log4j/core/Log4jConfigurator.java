package org.apache.logging.log4j.core;

import org.apache.logging.log4j.core.config.AbstractConfiguration;

/**
 * To use an implementation of <code>Log4jConfigurator</code>  
 * 
 * Multibinder<Log4jConfigurator> appenderBinder = Multibinder.newSetBinder(binder(), Log4jConfigurator.class);
 * appenderBinder.addBinding().to(Log4jConfiguratorImpl.class);
 * 
 * 
 */

public interface Log4jConfigurator {

    public void doConfigure(AbstractConfiguration config);

}
