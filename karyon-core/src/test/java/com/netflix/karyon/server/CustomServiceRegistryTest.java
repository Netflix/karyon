package com.netflix.karyon.server;

import com.netflix.karyon.server.bootstrap.AlwaysHealthyHealthCheck;
import com.netflix.karyon.server.bootstrap.DefaultBootstrap;
import com.netflix.karyon.server.bootstrap.ServiceRegistryClient;
import com.netflix.karyon.server.bootstrap.SyncHealthCheckInvocationStrategy;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * @author Nitesh Kant
 */
public class CustomServiceRegistryTest {

    @Test
    public void testCustomServiceRegistry() throws Exception {

        DefaultBootstrap.Builder builder = new DefaultBootstrap.Builder("test-servicereg", null);
        AlwaysHealthyHealthCheck healthCheckHandler = AlwaysHealthyHealthCheck.INSTANCE;
        builder.healthCheckHandler(healthCheckHandler)
               .healthCheckInvocationStrategy(new SyncHealthCheckInvocationStrategy(healthCheckHandler))
               .serviceRegistryClient(new InMemRegistry());
        DefaultBootstrap bootstrap = builder.build();
        KaryonServer server = new KaryonServer(bootstrap);
        server.start();

        assertTrue("Custom service client not called with status up", InMemRegistry.receivedUp);

        server.stop();

        assertTrue("Custom service client not called with status down", InMemRegistry.receivedDown);
    }

    public static class InMemRegistry implements ServiceRegistryClient {

        private static boolean receivedUp;
        private static boolean receivedDown;

        @Override
        public void start() {
            // No op
        }

        @Override
        public void updateStatus(ServiceStatus newStatus) {

            switch (newStatus) {
                case UP:
                    receivedUp = true;
                    break;
                case DOWN:
                    receivedDown = true;
                    break;
            }
        }
    }
}
