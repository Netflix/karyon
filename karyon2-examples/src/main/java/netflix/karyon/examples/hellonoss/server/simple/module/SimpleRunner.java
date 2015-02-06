package netflix.karyon.examples.hellonoss.server.simple.module;

import netflix.adminresources.resources.KaryonWebAdminModule;
import netflix.karyon.Karyon;
import netflix.karyon.KaryonBootstrapModule;
import netflix.karyon.ShutdownModule;
import netflix.karyon.archaius.ArchaiusBootstrapModule;
import netflix.karyon.servo.KaryonServoModule;

/**
 * @author Nitesh Kant
 */
public class SimpleRunner {

    public static void main(String[] args) {

        Karyon.forRequestHandler(8888,
                // new SimpleRouter(), /* Use this instead of RouterWithInterceptors below if interceptors are not required */
                new RouterWithInterceptors(),
                new KaryonBootstrapModule(),
                new ArchaiusBootstrapModule("hello-netflix-oss"),
                // KaryonEurekaModule.asBootstrapModule(), /* Uncomment if you need eureka */
                Karyon.toBootstrapModule(KaryonWebAdminModule.class),
                ShutdownModule.asBootstrapModule(),
                KaryonServoModule.asBootstrapModule())
                .startAndWaitTillShutdown();
    }
}
