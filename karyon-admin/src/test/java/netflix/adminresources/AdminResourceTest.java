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

package netflix.adminresources;

import com.google.inject.Provider;
import com.netflix.config.ConfigurationManager;
import netflix.karyon.health.AlwaysHealthyHealthCheck;
import netflix.karyon.health.HealthCheckHandler;
import netflix.karyon.health.HealthCheckInvocationStrategy;
import netflix.karyon.health.SyncHealthCheckInvocationStrategy;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Nitesh Kant
 */
public class AdminResourceTest {

    private AdminResourcesContainer container;

    @After
    public void tearDown() throws Exception {
        final AbstractConfiguration configInst = ConfigurationManager.getConfigInstance();
        configInst.clearProperty(AdminResourcesContainer.CONTAINER_LISTEN_PORT);
        container.shutdown();
    }

    @Before
    public void init() {
        System.setProperty(AdminResourcesContainer.CONTAINER_LISTEN_PORT, "0");
    }

    @Test
    public void testBasic() throws Exception {
        final int port = startServerAndGetListeningPort();
        HttpClient client = new DefaultHttpClient();
        HttpGet healthGet =
                new HttpGet(String.format("http://localhost:%d/healthcheck", port));
        HttpResponse response = client.execute(healthGet);
        Assert.assertEquals("admin resource health check failed.", 200, response.getStatusLine().getStatusCode());
    }

    private int startServerAndGetListeningPort() throws Exception {
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

        return container.getListenPort();
    }
}
