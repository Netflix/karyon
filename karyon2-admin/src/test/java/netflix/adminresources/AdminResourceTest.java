/*
 * Copyright 2014 Netflix, Inc.
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

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;

import netflix.admin.AdminConfigImpl;
import netflix.admin.RedirectRules;

public class AdminResourceTest {
    @Path("/ping")
    @Produces(MediaType.TEXT_HTML)
    public static class PingResource {
        @GET
        public Response ping() {
            return Response.ok().entity("pong").build();
        }
    }

    static class AdminResourcesFixture implements AutoCloseable {
        @Inject
        private AdminResourcesContainer container;
        @Inject
        @Named(KaryonAdminModule.ADMIN_RESOURCES_SERVER_PORT)
        private int serverPort;

        public void close() {
            container.shutdown();
        }
    }

    public AdminResourcesFixture adminTestFixture() throws Exception {
        final Injector appInjector = Guice.createInjector(new KaryonAdminModule() {
            @Override
            protected void configure() {
                bind(RedirectRules.class).toInstance(new RedirectRules() {
                    @Override
                    public Map<String, String> getMappings() {
                        Map<String, String> routes = new HashMap<>();
                        routes.put("/", "/bad-route");
                        routes.put("/check-me", "/jr/ping");
                        routes.put("/auth/ping", "/jr/ping");
                        return routes;
                    }

                    @Override
                    public String getRedirect(HttpServletRequest httpServletRequest) {
                        final String requestURI = httpServletRequest.getRequestURI();
                        if (requestURI.startsWith("/proxy-ping")) {
                            return "/jr/ping";
                        }
                        return null;
                    }
                });

            }
        });

        AdminResourcesContainer adminResourcesContainer = appInjector.getInstance(AdminResourcesContainer.class);
        adminResourcesContainer.init();

        return appInjector.getInstance(AdminResourcesFixture.class);
    }

    @BeforeClass
    public static void init() {
        setConfig(AdminConfigImpl.CONTAINER_LISTEN_PORT, "0");
        setConfig(AdminConfigImpl.NETFLIX_ADMIN_RESOURCE_CONTEXT, "/jr");
        setConfig(AdminConfigImpl.NETFLIX_ADMIN_TEMPLATE_CONTEXT, "/main");
    }

    @AfterClass
    public static void cleanup() {
        final AbstractConfiguration configInst = ConfigurationManager.getConfigInstance();
        configInst.clearProperty(AdminConfigImpl.CONTAINER_LISTEN_PORT);
    }

    private static void setConfig(String name, String value) {
        ConfigurationManager.getConfigInstance().setProperty(name, value);
    }

    private static void enableAdminConsole() {
        ConfigurationManager.getConfigInstance().setProperty(AdminConfigImpl.SERVER_ENABLE_PROP_NAME, true);
    }

    private static void disableAdminConsole() {
        ConfigurationManager.getConfigInstance().setProperty(AdminConfigImpl.SERVER_ENABLE_PROP_NAME, false);
    }

    @Test
    public void checkPing() throws Exception {
        try (AdminResourcesFixture testFixture = adminTestFixture()) {
            HttpClient client = new DefaultHttpClient();
            HttpGet healthGet = new HttpGet(String.format("http://localhost:%d/jr/ping", testFixture.serverPort));
            HttpResponse response = client.execute(healthGet);
            assertEquals("admin resource ping resource failed.", 200, response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void checkAuthFilter() throws Exception {
        setConfig(AdminConfigImpl.NETFLIX_ADMIN_CTX_FILTERS, "netflix.adminresources.AuthFilter");

        try (AdminResourcesFixture testFixture = adminTestFixture()) {
            HttpClient client = new DefaultHttpClient();

            HttpGet healthGet = new HttpGet(String.format("http://localhost:%d/jr/ping", testFixture.serverPort));
            HttpResponse response = client.execute(healthGet);
            assertEquals("admin resource ping resource failed.", 200, response.getStatusLine().getStatusCode());
            consumeResponse(response);

            healthGet = new HttpGet(String.format("http://localhost:%d/main/get-user-id", testFixture.serverPort));
            response = client.execute(healthGet);
            assertEquals("admin resource ping resource failed.", 403, response.getStatusLine().getStatusCode());
            consumeResponse(response);

            healthGet = new HttpGet(String.format("http://localhost:%d/auth/no-page", testFixture.serverPort));
            response = client.execute(healthGet);
            assertEquals("admin resource ping resource failed.", 404, response.getStatusLine().getStatusCode());
            consumeResponse(response);

            healthGet = new HttpGet(String.format("http://localhost:%d/foo/ping", testFixture.serverPort));
            response = client.execute(healthGet);
            assertEquals("admin resource ping resource failed.", 404, response.getStatusLine().getStatusCode());
            consumeResponse(response);

            // verify redirect filter gets applied
            healthGet = new HttpGet(String.format("http://localhost:%d/check-me", testFixture.serverPort));
            response = client.execute(healthGet);
            assertEquals("admin resource did not pick a custom redirect routing", 200,
                    response.getStatusLine().getStatusCode());
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            assertEquals("ping resource did not return pong", br.readLine(), "pong");

            // verify auth filter not applied to a potential redirect path
            healthGet = new HttpGet(String.format("http://localhost:%d/auth/ping", testFixture.serverPort));
            response = client.execute(healthGet);
            assertEquals("admin resource did not pick a custom redirect routing", 200,
                    response.getStatusLine().getStatusCode());
            br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            assertEquals("ping resource did not return pong", br.readLine(), "pong");

        } finally {
            final AbstractConfiguration configInst = ConfigurationManager.getConfigInstance();
            configInst.clearProperty(AdminConfigImpl.NETFLIX_ADMIN_CTX_FILTERS);
        }
    }

    @Test
    public void checkDefaultRedirectRule() throws Exception {
        try (AdminResourcesFixture testFixture = adminTestFixture()) {
            HttpClient client = new DefaultHttpClient();
            HttpGet rootGet = new HttpGet(String.format("http://localhost:%d/", testFixture.serverPort));
            HttpResponse response = client.execute(rootGet);
            assertEquals("admin resource root resource does not redirect to template root context.", 404,
                    response.getStatusLine().getStatusCode());
        }
    }

    @Test
    public void testEphemeralAdminResourcePort() throws Exception {
        try (AdminResourcesFixture testFixture = adminTestFixture()) {
            Assert.assertEquals(0,
                    ConfigurationManager.getConfigInstance().getInt(AdminConfigImpl.CONTAINER_LISTEN_PORT));
            Assert.assertTrue(testFixture.serverPort > 0);
            Assert.assertEquals(testFixture.serverPort, testFixture.container.getServerPort());
        }
    }

    @Test
    public void testServiceDisabledFlag() throws Exception {
        disableAdminConsole();
        try (AdminResourcesFixture testFixture = adminTestFixture()) {
            assertEquals("admin resource did not get disabled with a config flag", 0, testFixture.serverPort);
        }
        enableAdminConsole();
    }

    @Test
    public void checkCustomRedirectRule() throws Exception {
        try (AdminResourcesFixture testFixture = adminTestFixture()) {

            HttpClient client = new DefaultHttpClient();

            HttpGet rootGet = new HttpGet(String.format("http://localhost:%d/", testFixture.serverPort));
            HttpResponse response = client.execute(rootGet);
            assertEquals("admin resource did not execute custom redirect routing", 404,
                    response.getStatusLine().getStatusCode());
            consumeResponse(response);

            HttpGet healthGet = new HttpGet(String.format("http://localhost:%d/check-me", testFixture.serverPort));
            response = client.execute(healthGet);
            assertEquals("admin resource did not pick a custom redirect routing", 200,
                    response.getStatusLine().getStatusCode());
            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            assertEquals("ping resource did not return pong", br.readLine(), "pong");

            healthGet = new HttpGet(
                    String.format("http://localhost:%d/proxy-ping/or-not?when=some-day", testFixture.serverPort));
            response = client.execute(healthGet);
            assertEquals("admin resource did not pick a custom redirect routing", 200,
                    response.getStatusLine().getStatusCode());
            br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            assertEquals("ping resource did not return pong", br.readLine(), "pong");
        }
    }

    private void consumeResponse(HttpResponse response) throws IOException {
        if (response.getEntity() != null && response.getEntity().getContent() != null) {
            final BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            while (br.readLine() != null)
                ;
        }
    }
}
