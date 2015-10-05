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
import org.junit.BeforeClass;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AdminResourceTest {
    @Path("/ping")
    @Produces(MediaType.TEXT_HTML)
    public static class PingResource {
        @GET
        public Response ping() {
            return Response.ok().entity("pong").build();
        }
    }

    private AdminResourcesContainer container;

    @After
    public void tearDown() throws Exception {
        final AbstractConfiguration configInst = ConfigurationManager.getConfigInstance();
        configInst.clearProperty(AdminConfigImpl.CONTAINER_LISTEN_PORT);
        if (container != null) {
            container.shutdown();
        }
    }

    @BeforeClass
    public static void init() {
        setConfig(AdminConfigImpl.CONTAINER_LISTEN_PORT, "0");
        setConfig(AdminConfigImpl.NETFLIX_ADMIN_RESOURCE_CONTEXT, "/jr");
        setConfig(AdminConfigImpl.NETFLIX_ADMIN_TEMPLATE_CONTEXT, "/main");
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
        final int port = startServerAndGetListeningPort();
        HttpClient client = new DefaultHttpClient();
        HttpGet healthGet = new HttpGet(String.format("http://localhost:%d/jr/ping", port));
        HttpResponse response = client.execute(healthGet);
        assertEquals("admin resource ping resource failed.", 200, response.getStatusLine().getStatusCode());
    }

    @Test
    public void checkAuthFilter() throws Exception {
        setConfig(AdminConfigImpl.NETFLIX_ADMIN_CTX_FILTERS, "netflix.adminresources.AuthFilter");
        final int port = startServerAndGetListeningPort();
        HttpClient client = new DefaultHttpClient();

        HttpGet healthGet = new HttpGet(String.format("http://localhost:%d/jr/ping", port));
        HttpResponse response = client.execute(healthGet);
        assertEquals("admin resource ping resource failed.", 200, response.getStatusLine().getStatusCode());
        consumeResponse(response);

        healthGet = new HttpGet(String.format("http://localhost:%d/main/ping", port));
        response = client.execute(healthGet);
        assertEquals("admin resource ping resource failed.", 403, response.getStatusLine().getStatusCode());
        consumeResponse(response);

        healthGet = new HttpGet(String.format("http://localhost:%d/foo/ping", port));
        response = client.execute(healthGet);
        assertEquals("admin resource ping resource failed.", 404, response.getStatusLine().getStatusCode());

        final AbstractConfiguration configInst = ConfigurationManager.getConfigInstance();
        configInst.clearProperty(AdminConfigImpl.NETFLIX_ADMIN_CTX_FILTERS);
    }

    @Test
    public void checkDefaultRedirectRule() throws Exception {
        final int port = startServerAndGetListeningPort();
        HttpClient client = new DefaultHttpClient();
        HttpGet rootGet = new HttpGet(String.format("http://localhost:%d/", port));
        HttpResponse response = client.execute(rootGet);
        assertEquals("admin resource root resource does not redirect to template root context.", 404, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testServiceDisabledFlag() throws Exception {
        disableAdminConsole();
        final int port = startServerAndGetListeningPort();
        assertEquals("admin resource did not get disabled with a config flag", 0, port);
        enableAdminConsole();
    }

    @Test
    public void checkCustomRedirectRule() throws Exception {
        final AdminResourcesContainer customRedirectContainer = adminResourcesContainerWithCustomRedirect();
        customRedirectContainer.init();
        final int serverPort = customRedirectContainer.getServerPort();
        HttpClient client = new DefaultHttpClient();

        HttpGet rootGet = new HttpGet(String.format("http://localhost:%d/", serverPort));
        HttpResponse response = client.execute(rootGet);
        assertEquals("admin resource did not execute custom redirect routing", 404, response.getStatusLine().getStatusCode());
        consumeResponse(response);

        HttpGet healthGet = new HttpGet(String.format("http://localhost:%d/check-me", serverPort));
        response = client.execute(healthGet);
        assertEquals("admin resource did not pick a custom redirect routing", 200, response.getStatusLine().getStatusCode());
        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        assertEquals("ping resource did not return pong", br.readLine(), "pong");


        healthGet = new HttpGet(String.format("http://localhost:%d/proxy-ping/or-not?when=some-day", serverPort));
        response = client.execute(healthGet);
        assertEquals("admin resource did not pick a custom redirect routing", 200, response.getStatusLine().getStatusCode());
        br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        assertEquals("ping resource did not return pong", br.readLine(), "pong");

        customRedirectContainer.shutdown();
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
                        routes.put("/check-me", "/jr/ping");
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
        injectorField.setAccessible(true);
        injectorField.set(customRedirectContainer, appInjector);
        return customRedirectContainer;
    }

    private void consumeResponse(HttpResponse response) throws IOException {
        final BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        while (br.readLine() != null) ;
    }
}
