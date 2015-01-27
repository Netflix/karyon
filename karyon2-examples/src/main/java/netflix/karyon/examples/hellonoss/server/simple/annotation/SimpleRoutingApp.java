package netflix.karyon.examples.hellonoss.server.simple.annotation;

import com.google.inject.Singleton;
import com.netflix.governator.annotations.Modules;
import io.netty.buffer.ByteBuf;
import netflix.adminresources.resources.KaryonWebAdminModule;
import netflix.karyon.KaryonBootstrap;
import netflix.karyon.ShutdownModule;
import netflix.karyon.archaius.ArchaiusBootstrap;
import netflix.karyon.examples.hellonoss.common.LoggingInterceptor;
import netflix.karyon.examples.hellonoss.common.auth.AuthInterceptor;
import netflix.karyon.examples.hellonoss.common.auth.AuthenticationService;
import netflix.karyon.examples.hellonoss.common.auth.AuthenticationServiceImpl;
import netflix.karyon.examples.hellonoss.common.health.HealthCheck;
import netflix.karyon.examples.hellonoss.server.simple.SimpleRouter;
import netflix.karyon.examples.hellonoss.server.simple.annotation.SimpleRoutingApp.KaryonRxRouterModuleImpl;
import netflix.karyon.servo.KaryonServoModule;
import netflix.karyon.transport.http.KaryonHttpModule;

/**
 * @author Tomasz Bak
 */
@ArchaiusBootstrap
@KaryonBootstrap(name = "hello-netflix-oss", healthcheck = HealthCheck.class)
@Singleton
@Modules(include = {
        ShutdownModule.class,
        KaryonServoModule.class,
        KaryonWebAdminModule.class,
        // KaryonEurekaModule.class, // Uncomment this to enable Eureka client.
        KaryonRxRouterModuleImpl.class
})
public interface SimpleRoutingApp {

    class KaryonRxRouterModuleImpl extends KaryonHttpModule<ByteBuf, ByteBuf> {

        public KaryonRxRouterModuleImpl() {
            super("httpServerA", ByteBuf.class, ByteBuf.class);
        }

        @Override
        protected void configureServer() {
            bindRouter().toInstance(new SimpleRouter());

            bind(AuthenticationService.class).to(AuthenticationServiceImpl.class);
            interceptorSupport().forUri("/*").intercept(LoggingInterceptor.class);
            interceptorSupport().forUri("/hello").interceptIn(AuthInterceptor.class);

            server().port(8888);
        }
    }
}
