package netflix.karyon.examples.hellonoss.server.rxnetty;

import netflix.adminresources.resources.KaryonWebAdminModule;
import netflix.karyon.Karyon;
import netflix.karyon.KaryonBootstrapSuite;
import netflix.karyon.ShutdownModule;
import netflix.karyon.archaius.ArchaiusSuite;
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
                                 new KaryonBootstrapSuite(healthCheckHandler),
                                 new ArchaiusSuite("hello-netflix-oss"),
                                 // KaryonEurekaModule.asSuite(), /* Uncomment if you need eureka */
                                 KaryonWebAdminModule.asSuite(),
                                 ShutdownModule.asSuite(),
                                 KaryonServoModule.asSuite())
              .startAndWaitTillShutdown();
    }
}
