package netflix.karyon;

import com.google.inject.AbstractModule;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.netflix.governator.guice.LifecycleInjectorBuilder;
import com.netflix.governator.guice.LifecycleInjectorBuilderSuite;
import com.netflix.governator.guice.LifecycleInjectorMode;
import netflix.karyon.health.AlwaysHealthyHealthCheck;
import netflix.karyon.health.HealthCheckHandler;
import netflix.karyon.health.HealthCheckInvocationStrategy;
import netflix.karyon.health.SyncHealthCheckInvocationStrategy;

import javax.inject.Inject;

/**
 * A guice module that defines all bindings required by karyon. Applications must use this to bootstrap karyon.
 *
 * @author Nitesh Kant
 */
public class KaryonBootstrapSuite implements LifecycleInjectorBuilderSuite {

    private final Class<? extends HealthCheckHandler> healthcheckHandlerClass;
    private final HealthCheckHandler healthcheckHandler;

    public KaryonBootstrapSuite() {
        this((HealthCheckHandler)null);
    }

    public KaryonBootstrapSuite(HealthCheckHandler healthcheckHandler) {
        this.healthcheckHandler = null == healthcheckHandler ? new AlwaysHealthyHealthCheck() : healthcheckHandler;
        healthcheckHandlerClass = null;
    }

    @Inject
    KaryonBootstrapSuite(KaryonBootstrap karyonBootstrap) {
        healthcheckHandlerClass = karyonBootstrap.healthcheck();
        healthcheckHandler = null;
    }

    @Override
    public void configure(LifecycleInjectorBuilder builder) {
        builder.withMode(LifecycleInjectorMode.SIMULATED_CHILD_INJECTORS);
        builder.withAdditionalModules(new AbstractModule() {
            @Override
            protected void configure() {
                bindHealthCheck(bind(HealthCheckHandler.class));
                bindHealthCheckInvocationStrategy(bind(HealthCheckInvocationStrategy.class));
            }
        });
    }

    private static void bindHealthCheckInvocationStrategy(AnnotatedBindingBuilder<HealthCheckInvocationStrategy> bindingBuilder) {
        bindingBuilder.to(SyncHealthCheckInvocationStrategy.class);
    }

    protected void bindHealthCheck(LinkedBindingBuilder<HealthCheckHandler> bindingBuilder) {
        if (null != healthcheckHandlerClass) {
            bindingBuilder.to(healthcheckHandlerClass);
        } else {
            bindingBuilder.toInstance(healthcheckHandler);
        }
    }
}
