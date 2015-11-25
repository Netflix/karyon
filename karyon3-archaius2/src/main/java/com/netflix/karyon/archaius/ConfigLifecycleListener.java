package com.netflix.karyon.archaius;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.archaius.Config;
import com.netflix.karyon.DefaultLifecycleListener;

/**
 * Configuration listener that will print out the configuration if the injector failed to create
 * 
 * @author elandau
 */
@Singleton
public class ConfigLifecycleListener extends DefaultLifecycleListener {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigLifecycleListener.class);
    
    private final Config config;
    
    @Inject
    public ConfigLifecycleListener(Config config) {
        this.config = config;
    }

    @Override
    public void onStarted() {
        LOG.debug("Injector created with final configuration ");
        LOG.debug("========================================= ");
        config.accept(new SLF4JConfigVisitor());
    }
    
    @Override
    public void onStartFailed(Throwable t) {
        LOG.debug("Injector failed with final configuration ");
        LOG.debug("======================================== ");
        config.accept(new SLF4JConfigVisitor());
    }
}
