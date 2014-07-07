package com.netflix.karyon.server.governator;

import javax.inject.Inject;

import com.netflix.governator.guice.SimpleLifecycleInjectorBuilderSuite;

/**
 * Main karyon bootstrap module.  
 * 
 * @see {@link KaryonConfiguration}
 * @author elandau
 *
 */
public class KaryonBootstrap extends SimpleLifecycleInjectorBuilderSuite {

    private final KaryonConfiguration config;
    
    @Inject
    public KaryonBootstrap(KaryonConfiguration config) {
        this.config = config;
    }
    
    @Override
    public void configure() {
        install(new KaryonModule() {
            protected void configureKaryon() {
                bindServletContextListener().to(config.listener());
                bindHealthCheckHandler().to(config.healthcheck());
                bindHealthCheckInvocationStrategy().to(config.healthcheckStrategy());
            }
        });
    }

}
