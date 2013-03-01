package com.netflix.adminresources;

import com.google.inject.Injector;
import com.netflix.config.ConfigurationManager;
import com.netflix.karyon.server.KaryonServer;
import com.netflix.karyon.spi.PropertyNames;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Nitesh Kant
 */
public class AdminResourceTest {

    private KaryonServer server;

    @Before
    public void setUp() throws Exception {
        System.setProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE, "com.test");
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
        HttpGet healthGet = new HttpGet("http://localhost:8077/healthcheck");
        HttpResponse response = client.execute(healthGet);
        System.out.println("response = " + response);
    }

    private Injector startServer() throws Exception {
        server = new KaryonServer();
        Injector injector = server.initialize();
        server.start();
        return injector;
    }
}
