package netflix.karyon.examples.hellonoss.server.jersey;

import com.netflix.governator.annotations.Modules;
import netflix.adminresources.resources.KaryonWebAdminModule;
import netflix.karyon.KaryonBootstrap;
import netflix.karyon.ShutdownModule;
import netflix.karyon.archaius.ArchaiusBootstrap;
import netflix.karyon.examples.hellonoss.common.LoggingInterceptor;
import netflix.karyon.examples.hellonoss.common.auth.AuthInterceptor;
import netflix.karyon.examples.hellonoss.common.auth.AuthenticationService;
import netflix.karyon.examples.hellonoss.common.auth.AuthenticationServiceImpl;
import netflix.karyon.examples.hellonoss.common.health.HealthCheck;
import netflix.karyon.examples.hellonoss.server.jersey.JerseyHelloWorldApp.KaryonJerseyModuleImpl;
import netflix.karyon.jersey.blocking.KaryonJerseyModule;
import netflix.karyon.servo.KaryonServoModule;

@ArchaiusBootstrap
@KaryonBootstrap(name = "hello-netflix-oss", healthcheck = HealthCheck.class)
@Modules(include = {
        ShutdownModule.class,
        KaryonWebAdminModule.class,
        // KaryonEurekaModule.class, // Uncomment this to enable Eureka client.
        KaryonJerseyModuleImpl.class,
        KaryonServoModule.class
})
public interface JerseyHelloWorldApp {

    class KaryonJerseyModuleImpl extends KaryonJerseyModule {
        @Override
        protected void configureServer() {
            bind(AuthenticationService.class).to(AuthenticationServiceImpl.class);
            interceptorSupport().forUri("/*").intercept(LoggingInterceptor.class);
            interceptorSupport().forUri("/hello").interceptIn(AuthInterceptor.class);
            server().port(8888).threadPoolSize(100);
        }
    }
}
