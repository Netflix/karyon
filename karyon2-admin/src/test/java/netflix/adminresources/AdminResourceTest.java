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

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import netflix.admin.AdminConfigImpl;
import netflix.admin.RedirectRules;
import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * @author Nitesh Kant
 */
public class AdminResourceTest {

    private AdminResourcesContainer container;

    @After
    public void tearDown() throws Exception {
        final AbstractConfiguration configInst = ConfigurationManager.getConfigInstance();
        configInst.clearProperty(AdminResourcesContainer.CONTAINER_LISTEN_PORT);
        if (container != null) {
            container.shutdown();
        }
    }

    @Before
    public void init() {
        System.setProperty(AdminResourcesContainer.CONTAINER_LISTEN_PORT, "0");
    }

    @Test
    public void testDefaultHealthCheck() throws Exception {
        final int port = startServerAndGetListeningPort();
        HttpClient client = new DefaultHttpClient();
        HttpGet healthGet =
                new HttpGet(String.format("http://localhost:%d/" + AdminConfigImpl.HEALTH_CHECK_PATH_DEFAULT, port));
        HttpResponse response = client.execute(healthGet);
        assertEquals("admin resource health check failed.", 200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void checkDefaultRedirectRule() throws Exception {
        final int port = startServerAndGetListeningPort();
        HttpClient client = new DefaultHttpClient();
        HttpGet rootGet = new HttpGet(String.format("http://localhost:%d/", port));
        HttpResponse response = client.execute(rootGet);
        assertEquals("admin resource root resource unavailable.", 200, response.getStatusLine().getStatusCode());
        final BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        assertEquals("root resource redirects to health check", br.readLine(), "OK");
    }

    @Test
    public void checkCustomRedirectRule() throws Exception {
        final AdminResourcesContainer badRedirectContainer = adminResourcesContainerWithCustomRedirect();
        badRedirectContainer.init();
        final int serverPort = badRedirectContainer.getServerPort();
        HttpClient client = new DefaultHttpClient();

        HttpGet rootGet = new HttpGet(String.format("http://localhost:%d/", serverPort));
        HttpResponse response = client.execute(rootGet);
        assertEquals("admin resource did not execute bad redirect routing", 404, response.getStatusLine().getStatusCode());
        consumeResponse(response);

        HttpGet healthGet = new HttpGet(String.format("http://localhost:%d/health", serverPort));
        response = client.execute(healthGet);
        assertEquals("admin resource did not pick a custom health check path", 200, response.getStatusLine().getStatusCode());

        badRedirectContainer.shutdown();
    }


    private int startServerAndGetListeningPort() throws Exception {
        container = new AdminResourcesContainer();
        container.init();

        return container.getServerPort();
    }

    private AdminResourcesContainer adminResourcesContainerWithCustomRedirect() throws Exception {
        AdminResourcesContainer customRedirectContainer = new AdminResourcesContainer();
        final Field injectorField = AdminResourcesContainer.class.getDeclaredField("appInjector");
        final Injector appInjector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(RedirectRules.class).toInstance(new RedirectRules() {
                    @Override
                    public Map<String, String> getMappings() {
                        Map<String, String> routes = new HashMap<>();
                        routes.put("/", "/bad-route");
                        routes.put("/health", AdminConfigImpl.HEALTH_CHECK_PATH_DEFAULT);
                        return routes;
                    }
                });

            }
        });
        injectorField.setAccessible(true);
        injectorField.set(customRedirectContainer, appInjector);
        return customRedirectContainer;
    }

    private void consumeResponse(HttpResponse response) throws IOException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        while (br.readLine() != null);
    }
}
