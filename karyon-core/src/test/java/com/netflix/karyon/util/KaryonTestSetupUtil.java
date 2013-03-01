package com.netflix.karyon.util;

import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.server.KaryonServer;
import com.netflix.karyon.spi.PropertyNames;
import com.test.RegistrationSequence;

/**
 * @author Nitesh Kant
 */
public class KaryonTestSetupUtil {

    public static void setUp() throws Exception {
        System.setProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE, "com.test");
        System.setProperty(PropertyNames.DISABLE_EUREKA_INTEGRATION, "true");
    }

    public static void tearDown(KaryonServer server) throws Exception {
        ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.DISABLE_APPLICATION_DISCOVERY_PROP_NAME);
        ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.EXPLICIT_APPLICATION_CLASS_PROP_NAME);
        RegistrationSequence.reset();
        server.close();
    }

    public static Injector startServer(KaryonServer server) throws Exception {
        Injector injector = server.initialize();
        server.start();
        return injector;
    }
}
