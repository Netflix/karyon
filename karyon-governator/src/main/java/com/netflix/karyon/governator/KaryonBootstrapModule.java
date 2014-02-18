package com.netflix.karyon.governator;

import com.netflix.governator.configuration.ArchaiusConfigurationProvider;
import com.netflix.governator.guice.BootstrapBinder;
import com.netflix.governator.guice.BootstrapModule;

/**
 * Default {@link BootstrapModule} used by {@link KaryonGovernatorBootstrap}. <br/>
 *
 * @author Nitesh Kant
 */
class KaryonBootstrapModule implements BootstrapModule {

    @Override
    public void configure(BootstrapBinder binder) {
        binder.bindConfigurationProvider().to(ArchaiusConfigurationProvider.class);
    }
}
