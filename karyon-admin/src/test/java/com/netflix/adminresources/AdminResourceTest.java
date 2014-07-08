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

import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConfigurationManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Nitesh Kant
 */
public class AdminResourceTest {

    public static final String CUSTOM_LISTEN_PORT = "9999";
    private AdminResourcesContainer container;

    @After
    public void tearDown() throws Exception {
        ((ConcurrentCompositeConfiguration) ConfigurationManager.getConfigInstance())
                .clearOverrideProperty(AdminResourcesContainer.CONTAINER_LISTEN_PORT);
        container.shutdown();
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

    //@Test (expected = HttpHostConnectException.class)
    public void testCustomPort() throws Exception {
        ((ConcurrentCompositeConfiguration) ConfigurationManager.getConfigInstance()).setOverrideProperty(
                AdminResourcesContainer.CONTAINER_LISTEN_PORT, CUSTOM_LISTEN_PORT);
        startServer();
        HttpClient client = new DefaultHttpClient();
        HttpGet healthGet = new HttpGet("http://localhost:"+ AdminResourcesContainer.LISTEN_PORT_DEFAULT + "/healthcheck");
        client.execute(healthGet);
        throw new AssertionError("Admin container did not bind to the custom port " + CUSTOM_LISTEN_PORT +
                                 ", instead listened to default port: " + AdminResourcesContainer.LISTEN_PORT_DEFAULT);
    }

    private void startServer() throws Exception {
        container = new AdminResourcesContainer();
        container.init();
    }
}
