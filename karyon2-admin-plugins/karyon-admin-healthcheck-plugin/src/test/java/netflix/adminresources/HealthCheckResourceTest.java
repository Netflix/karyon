package netflix.adminresources;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.server.eureka.HealthCheckInvocationStrategy;
import com.netflix.karyon.server.eureka.SyncHealthCheckInvocationStrategy;
import com.netflix.karyon.spi.HealthCheckHandler;

import org.apache.commons.configuration.AbstractConfiguration;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import netflix.admin.AdminConfigImpl;

public class HealthCheckResourceTest {
    private AdminResourcesContainer container;

    @After
    public void tearDown() throws Exception {
        final AbstractConfiguration configInst = ConfigurationManager.getConfigInstance();
        configInst.clearProperty(AdminConfigImpl.CONTAINER_LISTEN_PORT);
        if (container != null) {
            container.shutdown();
        }
    }

    @Before
    public void init() {
        System.setProperty(AdminConfigImpl.CONTAINER_LISTEN_PORT, "0");
        System.setProperty(AdminConfigImpl.NETFLIX_ADMIN_RESOURCE_CONTEXT, "/jr");
    }

    @Test
    public void goodHealth() throws Exception {
        checkHealth(goodHealthHandler(), Response.Status.OK.getStatusCode());
    }

    @Test
    public void badHealth() throws Exception {
        checkHealth(badHealthHandler(), Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
    }


    private void checkHealth(HealthCheckHandler healthCheckHandler, int respStatus) throws Exception {
        final AdminResourcesContainer adminResourcesContainer = buildAdminResourcesContainer(healthCheckHandler);
        adminResourcesContainer.init();
        final int adminPort = adminResourcesContainer.getServerPort();

        HttpClient client = new DefaultHttpClient();
        HttpGet healthGet =
                new HttpGet(String.format("http://localhost:%d/jr/v2/healthcheck", adminPort));
        HttpResponse response = client.execute(healthGet);
        assertEquals("admin resource health check resource failed.", respStatus, response.getStatusLine().getStatusCode());

        adminResourcesContainer.shutdown();
    }


    private AdminResourcesContainer buildAdminResourcesContainer(final HealthCheckHandler healthCheckHandler) throws Exception {
        final Injector appInjector = Guice.createInjector(new AbstractModule() {
            @Override
            protected void configure() {
                bind(HealthCheckHandler.class).toInstance(healthCheckHandler);
                bind(HealthCheckInvocationStrategy.class).to(SyncHealthCheckInvocationStrategy.class);
            }
        });
        return appInjector.getInstance(AdminResourcesContainer.class);
    }

    private HealthCheckHandler goodHealthHandler() {
        return new HealthCheckHandler() {
            @Override
            public int getStatus() {
                return Response.Status.OK.getStatusCode();
            }
        };
    }

    private HealthCheckHandler badHealthHandler() {
        return new HealthCheckHandler() {
            @Override
            public int getStatus() {
                return Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
            }
        };
    }

}
