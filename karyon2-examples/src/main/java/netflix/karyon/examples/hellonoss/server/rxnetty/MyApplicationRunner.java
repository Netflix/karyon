package netflix.karyon.examples.hellonoss.server.rxnetty;

import com.google.inject.AbstractModule;
import netflix.adminresources.resources.KaryonWebAdminModule;
import netflix.karyon.Karyon;
import netflix.karyon.KaryonBootstrapModule;
import netflix.karyon.ShutdownModule;
import netflix.karyon.archaius.ArchaiusBootstrapModule;
import netflix.karyon.examples.hellonoss.common.health.HealthCheck;
import netflix.karyon.health.HealthCheckHandler;
import netflix.karyon.servo.KaryonServoModule;
import netflix.karyon.transport.http.health.HealthCheckEndpoint;

import javax.ws.rs.core.Response;

/**
 * @author Nitesh Kant
 */
public class MyApplicationRunner {

    public static class HealthCheckHandlerModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(HealthCheckHandler.class).toInstance(new HealthCheckHandler() {
                @Override
                public int getStatus() {
                    return Response.Status.OK.getStatusCode();
                }
            });
        }
    }

    public static void main(String[] args) {
        HealthCheck healthCheckHandler = new HealthCheck();
        Karyon.forRequestHandler(8888,
                new RxNettyHandler("/healthcheck",
                        new HealthCheckEndpoint(healthCheckHandler)),
                new KaryonBootstrapModule(healthCheckHandler),
                new ArchaiusBootstrapModule("hello-netflix-oss"),
                // KaryonEurekaModule.asBootstrapModule(), /* Uncomment if you need eureka */
                Karyon.toBootstrapModule(KaryonWebAdminModule.class),
                ShutdownModule.asBootstrapModule(),
                KaryonServoModule.asBootstrapModule(),
                Karyon.toBootstrapModule(HealthCheckHandlerModule.class)
        )
                .startAndWaitTillShutdown();
    }
}
