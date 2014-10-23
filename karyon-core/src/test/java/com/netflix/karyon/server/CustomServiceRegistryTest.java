package com.netflix.karyon.server;

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.spi.ServiceRegistryClient;
import com.netflix.karyon.util.KaryonTestSetupUtil;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author Nitesh Kant
 */
public class CustomServiceRegistryTest {

    @Test
    public void testCustomServiceRegistry() throws Exception {
        KaryonTestSetupUtil.setUp();
        CustomBootstrap bootstrap = new CustomBootstrap();
        ((ConcurrentCompositeConfiguration) ConfigurationManager.getConfigInstance())
        .setOverrideProperty(KaryonTestSetupUtil.CONTAINER_LISTEN_PORT, KaryonTestSetupUtil.TESTCASE_LISTEN_PORT);
        KaryonServer server = new KaryonServer(bootstrap);
        server.initialize();
        server.start();

        Assert.assertTrue("Custom service client not called with status up", InMemRegistry.receivedUp);

        KaryonTestSetupUtil.tearDown(server);
        ((ConcurrentCompositeConfiguration) ConfigurationManager.getConfigInstance())
        .clearProperty(KaryonTestSetupUtil.CONTAINER_LISTEN_PORT);

        Assert.assertTrue("Custom service client not called with status down", InMemRegistry.receivedDown);
    }

    public static class CustomBootstrap extends ServerBootstrap {

        @Override
        protected Class<? extends ServiceRegistryClient> getServiceRegistryClient() {
            return InMemRegistry.class;
        }
    }

    public static class InMemRegistry implements ServiceRegistryClient {

        private static boolean receivedUp;
        private static boolean receivedDown;

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
