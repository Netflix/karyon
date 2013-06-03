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

package com.netflix.karyon.server;

import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.server.eureka.AsyncHealthCheckInvocationStrategy;
import com.netflix.karyon.server.eureka.EurekaHealthCheckCallback;
import com.netflix.karyon.server.eureka.SyncHealthCheckInvocationStrategy;
import com.netflix.karyon.spi.PropertyNames;
import com.netflix.karyon.util.KaryonTestSetupUtil;
import com.test.FlappingHealthCheck;
import com.test.HealthCheckGuy;
import com.test.RegistrationSequence;
import com.test.RogueHealthCheck;
import com.test.TestApplication;
import com.test.TestComponent;
import com.testmulti.TestApplication2;
import com.testmulti.TestApplication3;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nitesh Kant
 */
public class KaryonServerTest {

    private static final Logger logger = LoggerFactory.getLogger(KaryonServerTest.class);
    private KaryonServer server;

    @Before
    public void setUp() throws Exception {
        KaryonTestSetupUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        KaryonTestSetupUtil.tearDown(server);
    }

    @Test
    public void testAnnotatedClasses() throws Exception {

        logger.error("Setting properties explicitly.");

        System.setProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE, "com.test");
        System.setProperty(PropertyNames.DISABLE_EUREKA_INTEGRATION, "true");


        startServer();

        Assert.assertTrue("Component not initialized.", RegistrationSequence.contains(TestComponent.class));
        Assert.assertTrue("Application not initialized.", RegistrationSequence.contains(TestApplication.class));
        Assert.assertTrue("Component not initialized before app.",
                RegistrationSequence.isBefore(TestComponent.class, TestApplication.class));
    }

    @Test
    public void testOnlyAnnotatedComponent() throws Exception {
        ConfigurationManager.getConfigInstance().setProperty(PropertyNames.DISABLE_APPLICATION_DISCOVERY_PROP_NAME,
                true);

        startServer();
        Assert.assertTrue("Component not initialized.", RegistrationSequence.contains(TestComponent.class));
        Assert.assertFalse("Application initialized.", RegistrationSequence.contains(TestApplication.class));
    }

    @Test
    public void testHealthCheck() throws Exception {
        ConfigurationManager.getConfigInstance().setProperty(PropertyNames.HEALTH_CHECK_HANDLER_CLASS_PROP_NAME,
                HealthCheckGuy.class.getName());

        Injector injector = startServer();
        injector.getInstance(EurekaHealthCheckCallback.class);
        Assert.assertTrue("Health check handler not initialized.", RegistrationSequence.contains(HealthCheckGuy.class));
    }

    @Test
    public void testHealthCheckTimeout() throws Exception {
        ConfigurationManager.getConfigInstance().setProperty(PropertyNames.HEALTH_CHECK_STRATEGY,
                AsyncHealthCheckInvocationStrategy.class.getName());

        ConfigurationManager.getConfigInstance().setProperty(PropertyNames.HEALTH_CHECK_HANDLER_CLASS_PROP_NAME,
                RogueHealthCheck.class.getName());

        Injector injector = startServer();
        EurekaHealthCheckCallback eurekaHealthCheckCallback = injector.getInstance(EurekaHealthCheckCallback.class);
        Assert.assertFalse("Health check did not timeout.", eurekaHealthCheckCallback.isHealthy());
    }

    @Test
    public void testFlappingHealthCheck() throws Exception {
        ConfigurationManager.getConfigInstance().setProperty(PropertyNames.HEALTH_CHECK_STRATEGY,
                SyncHealthCheckInvocationStrategy.class.getName());

        ConfigurationManager.getConfigInstance().setProperty(PropertyNames.HEALTH_CHECK_HANDLER_CLASS_PROP_NAME,
                FlappingHealthCheck.class.getName());

        Injector injector = startServer();
        EurekaHealthCheckCallback eurekaHealthCheckCallback = injector.getInstance(EurekaHealthCheckCallback.class);
        Assert.assertTrue("First health check did not pass.", eurekaHealthCheckCallback.isHealthy());
        Assert.assertFalse("Second health check did not fail.", eurekaHealthCheckCallback.isHealthy());
    }

    @Test
    public void testHealthCheckSuccess() throws Exception {
        ConfigurationManager.getConfigInstance().setProperty(PropertyNames.HEALTH_CHECK_HANDLER_CLASS_PROP_NAME,
                HealthCheckGuy.class.getName());

        Injector injector = startServer();
        EurekaHealthCheckCallback eurekaHealthCheckCallback = injector.getInstance(EurekaHealthCheckCallback.class);
        Assert.assertTrue("Health check failed.", eurekaHealthCheckCallback.isHealthy());
    }

    @Test
    public void testMultipleApps() throws Exception {
        ConfigurationManager.getConfigInstance().setProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE, "com.test,com.testmulti");
        ConfigurationManager.getConfigInstance().setProperty(PropertyNames.EXPLICIT_APPLICATION_CLASS_PROP_NAME,
                TestApplication3.class.getName());
        startServer();

        Assert.assertTrue("Component not initialized.", RegistrationSequence.contains(TestComponent.class));
        Assert.assertTrue("Application 3 not initialized.", RegistrationSequence.contains(TestApplication3.class));
        Assert.assertTrue("Component not initialized before app.",
                RegistrationSequence.isBefore(TestComponent.class, TestApplication.class));
        Assert.assertFalse("Application initialized.", RegistrationSequence.contains(TestApplication.class));
        Assert.assertFalse("Application 2 initialized.", RegistrationSequence.contains(TestApplication2.class));
    }

    private Injector startServer() throws Exception {
        server = new KaryonServer();
        return KaryonTestSetupUtil.startServer(server);
    }
}
