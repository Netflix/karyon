package com.netflix.karyon.server;

import com.google.inject.Injector;
import com.netflix.karyon.util.KaryonTestSetupUtil;
import com.test.Bootstrap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

/**
 * @author Nitesh Kant
 */
public class BootstrapCallbacksTest {

    private KaryonServer server;
    private Bootstrap bootstrap;

    @Before
    public void setUp() throws Exception {
        KaryonTestSetupUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        KaryonTestSetupUtil.tearDown(server);
    }

    @Test
    public void testCallbacks() throws Exception {
        startServer();
        Set<String> callbacksInvoked = bootstrap.getCallbacksInvoked();
        assertCallbackInvoked(callbacksInvoked, Bootstrap.BEFORE_INJECTION_CREATION);
        assertCallbackInvoked(callbacksInvoked, Bootstrap.CREATE_INJECTOR);
    }

    private static void assertCallbackInvoked(Set<String> callbacksInvoked, String callbackname) {
        Assert.assertTrue("Bootstrap callback: " + callbackname + " not invoked by karyon.", callbacksInvoked.contains(
                callbackname));
    }

    private Injector startServer() throws Exception {
        bootstrap = new Bootstrap();
        server = new KaryonServer(bootstrap);
        return KaryonTestSetupUtil.startServer(server, bootstrap);
    }
}
