package com.netflix.karyon.server;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.DiscoveryManager;
import com.netflix.karyon.spi.PropertyNames;
import com.netflix.karyon.util.EurekaResourceMock;
import org.junit.After;
import org.junit.Before;

/**
 * @author Nitesh Kant (nkant@netflix.com)
 */
public class EurekaIntegrationTest {

    private KaryonServer server;
    private EurekaResourceMock eurekaResourceMock;

    @Before
    public void setUp() throws Exception {
        System.setProperty(PropertyNames.SERVER_BOOTSTRAP_BASE_PACKAGES_OVERRIDE, "com.test");
        System.setProperty(PropertyNames.KARYON_PROPERTIES_PREFIX + PropertyNames.EUREKA_COMPONENT_NAME + ".disable", "false");
        System.setProperty("eureka.name", EurekaResourceMock.EUREKA_KARYON_APP_NAME);
        System.setProperty("eureka.shouldUseDns", "false");
        System.setProperty("eureka.disableDelta", "true");
        System.setProperty("eureka.vipAddress", EurekaResourceMock.EUREKA_KARYON_VIP);
        System.setProperty("eureka.port", "8080");
        System.setProperty("eureka.serviceUrl.default", EurekaResourceMock.EUREKA_SERVICE_URL);
        eurekaResourceMock = new EurekaResourceMock();
        eurekaResourceMock.start();
    }

    @After
    public void tearDown() throws Exception {
        eurekaResourceMock.stop();
    }

    /*@Test*/
    public void testRegister() throws Exception {
        startServer();
        InstanceInfo nextServerInfo = null;
        int retryCount = 0;
        int sleepTime = 30000;
        while (nextServerInfo == null && ++retryCount < 10) {
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

        retryCount = 0;
        while (nextServerInfo != null && ++retryCount < 10) {
            try {
                nextServerInfo = DiscoveryManager.getInstance()
                                                 .getDiscoveryClient()
                                                 .getNextServerFromEureka(System.getProperty("eureka.vipAddress"), false);
                System.out.println("Waiting for service to unregister with eureka.. Sleeping for: (ms)" + sleepTime);
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e1) {
                    System.out.println("Test interrupted while waiting for registry. Bailing out.");
                    break;
                }
            } catch (Throwable th) {
                System.out.println("Service unregistered with eureka after retries: " + retryCount);
            }
        }

    }

    private void startServer() throws Exception {
        server = new KaryonServer() { };
        server.initialize();
        server.start();
    }

    private void shutdownServer() throws Exception {
        server.close();
    }
}
