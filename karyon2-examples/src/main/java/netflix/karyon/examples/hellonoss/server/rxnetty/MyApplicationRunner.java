package netflix.karyon.examples.hellonoss.server.rxnetty;

import netflix.adminresources.resources.KaryonWebAdminModule;
import netflix.karyon.Karyon;
import netflix.karyon.KaryonBootstrapModule;
import netflix.karyon.ShutdownModule;
import netflix.karyon.archaius.ArchaiusBootstrapModule;
import netflix.karyon.examples.hellonoss.common.health.HealthCheck;
import netflix.karyon.servo.KaryonServoModule;
import netflix.karyon.transport.http.health.HealthCheckEndpoint;

/**
 * @author Nitesh Kant
 */
public class MyApplicationRunner {

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
                KaryonServoModule.asBootstrapModule()
        ).startAndWaitTillShutdown();
    }
}
