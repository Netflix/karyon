/*
 * Copyright 2013 Netflix, Inc.
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 */

package com.netflix.karyon.util;

import com.google.inject.Injector;
import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.server.KaryonServer;
import com.netflix.karyon.spi.PropertyNames;
import com.test.RegistrationSequence;

/**
 * @author Nitesh Kant
 */
public class KaryonTestSetupUtil {
	// Avoid using 8077 as some test servers already have Karyon based sidecars listening on port 8077
	public static final String TESTCASE_LISTEN_PORT = "18077";
	// Included here so not everyone has to depend on karyon-admin com.netflix.adminresources.AdminResourcesContainer
	public static final String CONTAINER_LISTEN_PORT = "netflix.platform.admin.resources.port";

    public static void setUp() throws Exception {
        System.setProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE, "com.test");
        System.setProperty(PropertyNames.DISABLE_EUREKA_INTEGRATION, "true");
    }

    public static void tearDown(KaryonServer server) throws Exception {
        ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE);
        ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.DISABLE_APPLICATION_DISCOVERY_PROP_NAME);
        ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.EXPLICIT_APPLICATION_CLASS_PROP_NAME);
        clearOverrideProperties(CONTAINER_LISTEN_PORT);
        RegistrationSequence.reset();
        server.close();
    }

    public static Injector startServer(KaryonServer server) throws Exception {
    	setOverrideProperty(CONTAINER_LISTEN_PORT, TESTCASE_LISTEN_PORT);
        Injector injector = server.initialize();
        server.start();
        return injector;
    }

    public static void clearOverrideProperties(String name) {
        ((ConcurrentCompositeConfiguration) ConfigurationManager.getConfigInstance()).clearOverrideProperty(name);
    }

    public static <T> void setOverrideProperty(String name, T value) {
        ((ConcurrentCompositeConfiguration) ConfigurationManager.getConfigInstance())
                .setOverrideProperty(name, value);
    }

}
