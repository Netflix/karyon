package com.netflix.karyon.examples.hellonoss.server.rxnetty;

import com.netflix.adminresources.resources.KaryonWebAdminModule;
import com.netflix.karyon.Karyon;
import com.netflix.karyon.ShutdownModule;
import com.netflix.karyon.archaius.ArchaiusSuite;
import com.netflix.karyon.examples.hellonoss.common.health.HealthCheck;
import com.netflix.karyon.servo.KaryonServoModule;
import com.netflix.karyon.transport.http.health.HealthCheckEndpoint;

/**
 * @author Nitesh Kant
 */
public class MyApplicationRunner {

    public static void main(String[] args) {
        Karyon.forRequestHandler(8888,
                                 new RxNettyHandler("/healthcheck",
                                                    new HealthCheckEndpoint(new HealthCheck())),
                                 new ArchaiusSuite("hello-netflix-oss"),
                                 // KaryonEurekaModule.asSuite(), /* Uncomment if you need eureka */
                                 KaryonWebAdminModule.asSuite(),
                                 ShutdownModule.asSuite(),
                                 KaryonServoModule.asSuite())
              .startAndWaitTillShutdown();
    }
}
