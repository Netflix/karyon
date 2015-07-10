package com.netflix.karyon.archaius;

import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.archaius.Config;
import com.netflix.archaius.visitor.PrintStreamVisitor;
import com.netflix.governator.DefaultLifecycleListener;

/**
 * Configuration listener that will print out the configuration if the injector failed to create
 * 
 * @author elandau
 */
@Singleton
public class ConfigLifecycleListener extends DefaultLifecycleListener {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigLifecycleListener.class);
    
    private Config config;

    public void setConfig(Config config) {
        this.config = config;
    }
    
    @Override
    public void onStartFailed(Throwable t) {
        LOG.info("Injector failed with final configuration ");
        config.accept(new PrintStreamVisitor());
    }
}
