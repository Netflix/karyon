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
        ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE);
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
