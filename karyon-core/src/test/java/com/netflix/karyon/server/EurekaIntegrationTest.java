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

import com.netflix.appinfo.InstanceInfo;
import com.netflix.config.ConfigurationManager;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.karyon.spi.PropertyNames;
import com.netflix.karyon.util.EurekaResourceMock;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Nitesh Kant
 */
public class EurekaIntegrationTest {

    private KaryonServer server;
    private EurekaResourceMock eurekaResourceMock;

    @Before
    public void setUp() throws Exception {
        System.setProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE, "com.test");
        System.setProperty(PropertyNames.DISABLE_EUREKA_INTEGRATION, "false");
        System.setProperty("eureka.name", EurekaResourceMock.EUREKA_KARYON_APP_NAME);
        System.setProperty("eureka.shouldUseDns", "false");
        System.setProperty("eureka.disableDelta", "true");
        System.setProperty("eureka.vipAddress", EurekaResourceMock.EUREKA_KARYON_VIP);
        System.setProperty("eureka.port", "8080");
        System.setProperty("eureka.serviceUrl.default", EurekaResourceMock.EUREKA_SERVICE_URL);
        System.setProperty("eureka.appinfo.replicate.interval", "1");
        System.setProperty("eureka.client.refresh.interval", "1");
        System.setProperty("eureka.lease.renewalInterval", "1");
        eurekaResourceMock = new EurekaResourceMock();
        eurekaResourceMock.start();
    }

    @After
    public void tearDown() throws Exception {
        eurekaResourceMock.stop();
        ConfigurationManager.getConfigInstance().setProperty(PropertyNames.DISABLE_EUREKA_INTEGRATION, "true");
    }

    @Test
    public void testRegister() throws Exception {
        startServer();
        InstanceInfo nextServerInfo = null;
        int retryCount = 0;
        int sleepTime = 1000;
        while (nextServerInfo == null && ++retryCount < 7) {
            try {
                nextServerInfo = DiscoveryManager.getInstance()
                                                 .getDiscoveryClient()
                                                 .getNextServerFromEureka(System.getProperty("eureka.vipAddress"), false);
                System.out.println("Service registered with eureka after retries: " + retryCount);
            } catch (Throwable th) {
                System.out.println("Waiting for service to register with eureka.. Sleeping for: (ms)" + sleepTime);
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e1) {
                    System.out.println("Test interrupted while waiting for registry. Bailing out.");
                    break;
                }
            }
        }

        shutdownServer();

        Assert.assertFalse("Application not unregistered from eureka.", eurekaResourceMock.handler.appRegistered.get());
    }

    private void startServer() throws Exception {
        server = new KaryonServer();
        server.initialize();
        server.start();
    }

    private void shutdownServer() throws Exception {
        server.close();
    }
}
