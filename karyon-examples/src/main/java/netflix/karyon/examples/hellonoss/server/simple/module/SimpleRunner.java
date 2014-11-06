package netflix.karyon.examples.hellonoss.server.simple.module;

import netflix.adminresources.resources.KaryonWebAdminModule;
import netflix.karyon.Karyon;
import netflix.karyon.ShutdownModule;
import netflix.karyon.archaius.ArchaiusSuite;
import netflix.karyon.servo.KaryonServoModule;

/**
 * @author Nitesh Kant
 */
public class SimpleRunner {

    public static void main(String[] args) {

        Karyon.forRequestHandler(8888,
                                 // new SimpleRouter(), /* Use this instead of RouterWithInterceptors below if interceptors are not required */
                                 new RouterWithInterceptors(),
                                 new ArchaiusSuite("hello-netflix-oss"),
                                 // KaryonEurekaModule.asSuite(), /* Uncomment if you need eureka */
                                 KaryonWebAdminModule.asSuite(),
                                 ShutdownModule.asSuite(),
                                 KaryonServoModule.asSuite())
              .startAndWaitTillShutdown();
    }
}
