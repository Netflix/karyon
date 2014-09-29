package com.netflix.karyon.examples.hellonoss.server.simple.module;

import com.netflix.adminresources.resources.KaryonWebAdminModule;
import com.netflix.karyon.Karyon;
import com.netflix.karyon.ShutdownModule;
import com.netflix.karyon.archaius.ArchaiusSuite;
import com.netflix.karyon.servo.KaryonServoModule;

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
