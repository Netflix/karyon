package netflix.karyon;

import com.google.inject.AbstractModule;
import com.google.inject.binder.AnnotatedBindingBuilder;
import com.google.inject.binder.LinkedBindingBuilder;
import com.netflix.governator.guice.*;
import netflix.karyon.health.AlwaysHealthyHealthCheck;
import netflix.karyon.health.HealthCheckHandler;
import netflix.karyon.health.HealthCheckInvocationStrategy;
import netflix.karyon.health.SyncHealthCheckInvocationStrategy;

import javax.inject.Inject;

/**
 * A guice module that defines all bindings required by karyon. Applications must use this to bootstrap karyon.
 *
 * @author Nitesh Kant
 * @deprecated 2016-07-20 Karyon2 no longer supported.  See https://github.com/Netflix/karyon/issues/347 for more info
 */
@Deprecated
public class KaryonBootstrapModule implements BootstrapModule {

    private final Class<? extends HealthCheckHandler> healthcheckHandlerClass;
    private final HealthCheckHandler healthcheckHandler;
    private final KaryonBootstrap karyonBootstrap;

    public KaryonBootstrapModule() {
        this((HealthCheckHandler)null);
    }

    public KaryonBootstrapModule(HealthCheckHandler healthcheckHandler) {
        this.healthcheckHandler = null == healthcheckHandler ? new AlwaysHealthyHealthCheck() : healthcheckHandler;
        this.healthcheckHandlerClass = null;
        this.karyonBootstrap = null;
    }

    public KaryonBootstrapModule(Class<? extends HealthCheckHandler> healthcheckHandlerClass) {
        this.healthcheckHandler = null;
        this.healthcheckHandlerClass = healthcheckHandlerClass;
        this.karyonBootstrap = null;
    }

    @Inject
    KaryonBootstrapModule(KaryonBootstrap karyonBootstrap) {
        this.karyonBootstrap = karyonBootstrap;
        this.healthcheckHandlerClass = karyonBootstrap.healthcheck();
        this.healthcheckHandler = null;
    }

    @Override
    public void configure(BootstrapBinder bootstrapBinder) {
        bootstrapBinder.inMode(LifecycleInjectorMode.SIMULATED_CHILD_INJECTORS);
        bootstrapBinder.include(new AbstractModule() {
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
