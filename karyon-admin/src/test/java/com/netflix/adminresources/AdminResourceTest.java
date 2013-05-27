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

package com.netflix.adminresources;

import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.server.KaryonServer;
import com.netflix.karyon.server.eureka.SyncHealthCheckInvocationStrategy;
import com.netflix.karyon.spi.PropertyNames;
import junit.framework.Assert;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Nitesh Kant
 */
public class AdminResourceTest {

    public static final String CUSTOM_LISTEN_PORT = "9999";
    private KaryonServer server;

    @Before
    public void setUp() throws Exception {
        System.setProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE, "com.test");
        System.setProperty(PropertyNames.HEALTH_CHECK_TIMEOUT_MILLIS, "60000");
        System.setProperty(PropertyNames.HEALTH_CHECK_STRATEGY, SyncHealthCheckInvocationStrategy.class.getName());
        System.setProperty(PropertyNames.DISABLE_EUREKA_INTEGRATION, "true");
    }

    @After
    public void tearDown() throws Exception {
        ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.DISABLE_APPLICATION_DISCOVERY_PROP_NAME);
        ConfigurationManager.getConfigInstance().clearProperty(PropertyNames.EXPLICIT_APPLICATION_CLASS_PROP_NAME);
        server.close();
    }

    @Test
    public void testBasic() throws Exception {
        startServer();
        HttpClient client = new DefaultHttpClient();
        HttpGet healthGet =
                new HttpGet("http://localhost:" + AdminResourcesContainer.LISTEN_PORT_DEFAULT + "/healthcheck");
        HttpResponse response = client.execute(healthGet);
        Assert.assertEquals("admin resource health check failed.", 200, response.getStatusLine().getStatusCode());
    }

    @Test (expected = HttpHostConnectException.class)
    public void testCustomPort() throws Exception {
        ConfigurationManager.getConfigInstance().setProperty(AdminResourcesContainer.CONTAINER_LISTEN_PORT, CUSTOM_LISTEN_PORT);
        startServer();
        HttpClient client = new DefaultHttpClient();
        HttpGet healthGet = new HttpGet("http://localhost:"+ AdminResourcesContainer.LISTEN_PORT_DEFAULT + "/healthcheck");
        client.execute(healthGet);
        throw new AssertionError("Admin container did not bind to the custom port " + CUSTOM_LISTEN_PORT +
                                 ", instead listened to default port: " + AdminResourcesContainer.LISTEN_PORT_DEFAULT);
    }

    private Injector startServer() throws Exception {
        server = new KaryonServer();
        Injector injector = server.initialize();
        server.start();
        return injector;
    }
}
