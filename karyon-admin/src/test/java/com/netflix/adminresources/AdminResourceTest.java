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

import com.google.inject.Provider;
import com.netflix.config.ConcurrentCompositeConfiguration;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.health.AlwaysHealthyHealthCheck;
import com.netflix.karyon.health.HealthCheckHandler;
import com.netflix.karyon.health.HealthCheckInvocationStrategy;
import com.netflix.karyon.health.SyncHealthCheckInvocationStrategy;
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

    private void startServer() throws Exception {
        container = new AdminResourcesContainer(new Provider<HealthCheckInvocationStrategy>() {
            @Override
            public HealthCheckInvocationStrategy get() {
                return new SyncHealthCheckInvocationStrategy(AlwaysHealthyHealthCheck.INSTANCE);
            }
        }, new Provider<HealthCheckHandler>() {
            @Override
            public HealthCheckHandler get() {
                return AlwaysHealthyHealthCheck.INSTANCE;
            }
        });
        container.init();
    }
}
