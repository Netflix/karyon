package com.netflix.karyon;

import javax.inject.Inject;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.ProvisionException;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.netflix.governator.guice.BootstrapBinder;
import com.netflix.governator.guice.BootstrapModule;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;
import com.netflix.governator.guice.LifecycleInjectorMode;
import com.netflix.karyon.health.HealthCheckHandler;
import com.netflix.karyon.health.HealthCheckInvocationStrategy;
import com.netflix.karyon.health.SyncHealthCheckInvocationStrategy;

/**
 * A guice module that defines all bindings required by karyon. Applications must use this to bootstrap karyon.
 *
 * @author Nitesh Kant
 */
public class KaryonBootstrapSuite implements LifecycleInjectorBuilderSuite {

    private final KaryonBootstrap karyonBootstrap;
    private final Injector injector;
    
    @Inject
    public KaryonBootstrapSuite(Injector injector, KaryonBootstrap karyonBootstrap) {
        this.karyonBootstrap = karyonBootstrap;
        this.injector = injector;
    }

    @Override
    public void configure(LifecycleInjectorBuilder builder) {
        builder.withMode(LifecycleInjectorMode.SIMULATED_CHILD_INJECTORS);
        builder.withAdditionalBootstrapModules(new BootstrapModule() {
            @Override
            public void configure(BootstrapBinder bootstrapBinder) {
                bootstrapBinder.bind(KaryonBootstrap.class).toInstance(karyonBootstrap);
            }
        });
        builder.withAdditionalModules(new AbstractModule() {
            @Override
            protected void configure() {
                bindHealthCheck(bind(HealthCheckHandler.class));
                bindHealthCheckInvocationStrategy(bind(HealthCheckInvocationStrategy.class));
            }
        });
        
        for (Class<? extends LifecycleInjectorBuilderSuite> suite : karyonBootstrap.suites()) {
            try {
                injector.getInstance(suite).configure(builder);
            } catch (Exception e) {
                throw new ProvisionException("Unable to invoke suite '" + suite.getName() + "'", e);
            }
        }
    }

    private static void bindHealthCheckInvocationStrategy(AnnotatedBindingBuilder<HealthCheckInvocationStrategy> bindingBuilder) {
        bindingBuilder.to(SyncHealthCheckInvocationStrategy.class);
    }

    protected void bindHealthCheck(LinkedBindingBuilder<HealthCheckHandler> bindingBuilder) {
        bindingBuilder.to(karyonBootstrap.healthcheck());
    }
}
